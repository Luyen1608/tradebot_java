package luyen.tradebot.Trade.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.PlaceOrderRequest;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;
import luyen.tradebot.Trade.util.ValidateRepsone;
import luyen.tradebot.Trade.util.enumTraderBot.ActionSystem;
import luyen.tradebot.Trade.util.enumTraderBot.PayloadType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;


@Slf4j
@Data
@ClientEndpoint
public class CTraderConnection {
    private UUID accountId;
    private String accessToken;
    private Double volumeMultiplier;

    private KafkaProducerService kafkaProducerService;
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${tradebot.prefix}")
    private String prefix = "trade365_";
    private ScheduledExecutorService pingScheduler;
    private String clientId;
    private String secretId;
    private String wsUrl;
    private Session session;
    private CTraderConnectionService connectionService;
    private boolean connected = false;
    private CompletableFuture<String> responseFuture; // Lưu trữ phản hồi từ WebSocket
    private int authenticatedTraderAccountId;
    private String accountType;
    //    private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();
    private boolean manualDisconnect = false;
    private ActionSystem actionSystem = ActionSystem.AUTH;

    public CTraderConnection(UUID accountId, String clientId, String secretId, String accessToken,
                             CTraderConnectionService connectionService, String wsUrl,
                             KafkaTemplate<String, String> kafkaTemplate, KafkaProducerService kafkaProducerService,
                             String prefix, Double volumeMultiplier, int ctidTraderAccountId) {
        this.accountId = accountId;
        this.kafkaTemplate = kafkaTemplate;
        this.accessToken = accessToken;
        this.connectionService = connectionService;
        this.wsUrl = wsUrl;
        this.clientId = clientId;
        this.secretId = secretId;
        this.volumeMultiplier = volumeMultiplier;
        this.authenticatedTraderAccountId = ctidTraderAccountId;
        this.kafkaProducerService = kafkaProducerService;
        if (prefix != null) {
            this.prefix = prefix;
        }
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, URI.create(wsUrl));
            actionSystem = ActionSystem.AUTH;
            log.info("WebSocket connection initiated at {}, waiting for onOpen event...", wsUrl);
        } catch (Exception e) {
            log.error("Failed to connect to cTrader WebSocket at {}", wsUrl, e);
            throw new RuntimeException("WebSocket connection failed", e);
        }
    }

    public boolean isConnectionSuccessful() {
        return session != null && session.isOpen() && connected;
    }

    /**
     * +     * Kiểm tra xem kết nối đã được xác thực ứng dụng chưa
     * +     * @return true nếu kết nối đã được xác thực ứng dụng
     * +
     */
    public boolean isApplicationAuthenticated() {
        return isConnectionSuccessful() && connected;
    }

    public void disconnect() {
        //create function disconnect
        if (session != null && session.isOpen()) {
            try {
                this.manualDisconnect = true;
                session.close();
                connected = false;
                log.info("Disconnected from cTrader WebSocket with Account Id: {}", accountId);
            } catch (Exception e) {
                log.error("Error disconnecting from cTrader WebSocket", e);
            }
            stopPingScheduler();
        }

//        if (webSocketSession != null && webSocketSession.isOpen()) {
//            try {
//                webSocketSession.close();
//                connected = false;
//                log.info("Disconnected from cTrader WebSocket");
//            } catch (Exception e) {
//                log.error("Error disconnecting from cTrader WebSocket", e);
//            }
//        }
    }

    private void sendAuthMessage() {
        // Implement authentication with access token
        actionSystem = ActionSystem.AUTH;
        String authMessage = String.format(
                "{\"clientMsgId\": \"%s\",\"payloadType\": 2100,\"payload\": {\"clientId\": \"%s\",\"clientSecret\": \"%s\"}}",
                generateClientMsgId(), clientId, secretId
        );

        sendRequest(authMessage);
    }

    public CompletableFuture<String> placeOrder(PlaceOrderRequest request) {

        String orderMessage = createOrderMessage(request);

//        CompletableFuture<String> future = new CompletableFuture<>();
//        pendingRequests.put(request.getClientMsgId(), future);
        actionSystem = ActionSystem.ORDER;
//        sendRequest(orderMessage);
        return sendRequest(orderMessage);
    }

    public CompletableFuture<String> closePosition(String clientMsgId, int positionId, int volume, PayloadType payloadType) {
        // Create ProtoOAClosePositionReq message
        String closeMessage = createClosePositionMessage(clientMsgId, positionId, volume, payloadType);
        actionSystem = ActionSystem.ORDER;
        return sendRequest(closeMessage);
    }

    private String createGetAccountListMessage() {
        return String.format(
                "{\"clientMsgId\": \"%s\",\"payloadType\": 2149,\"payload\": {\"accessToken\": \"%s\"}}",
                generateClientMsgId(), accessToken
        );
    }

    private String createAuthenticateTraderAccountMessage(int ctidTraderAccountId) {
        return String.format(
                "{\"clientMsgId\": \"%s\",\"payloadType\": 2102,\"payload\": {\"ctidTraderAccountId\": %d,\"accessToken\": \"%s\"}}",
                generateClientMsgId(), ctidTraderAccountId, accessToken
        );
    }

    // tạo message ProtoOAAmendPositionSLTPReq giống createOrderMessage (tài liệu tham khảo https://help.ctrader.com/open-api/messages/#protooaamendpositionsltpreq)
    private String createAmendPositionMessage(String positionId, BigDecimal stopLoss, BigDecimal takeProfit) {
        // This is a simplified version - in real implementation, use protobuf
        return String.format(
                "{\"clientMsgId\": \"%s\",\"payloadType\": 2114,\"payload\": {\"ctidTraderAccountId\": %d,\"positionId\": \"%s\",\"stopLoss\": %s,\"takeProfit\": %s}}",
                generateClientMsgId(), authenticatedTraderAccountId, positionId, stopLoss, takeProfit
        );
    }

    private String createHeartbeatMessage() {
        // This is a simplified version - in real implementation, use protobuf
        // JSON format for order placement
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"clientMsgId\": \"").append(generateClientMsgId()).append("\",");
        jsonBuilder.append("\"payloadType\": 51");

        jsonBuilder.append("}");

        return jsonBuilder.toString();
    }

    private String createOrderMessage(PlaceOrderRequest request) {
        // This is a simplified version - in real implementation, use protobuf
        // JSON format for order placement
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"clientMsgId\": \"").append(request.getClientMsgId()).append("\",");
//        jsonBuilder.append("\"payloadType\": 2106,");
        jsonBuilder.append("\"payloadType\": ").append(request.getPayloadType().getValue()).append(",");
        jsonBuilder.append("\"payload\": {");
        jsonBuilder.append("\"ctidTraderAccountId\": ").append(authenticatedTraderAccountId).append(",");
        jsonBuilder.append("\"symbolId\": ").append(request.getSymbol()).append(",");
        jsonBuilder.append("\"tradeSide\": ").append(request.getTradeSide()).append(",");
        jsonBuilder.append("\"orderType\": ").append(request.getOrderType()).append(",");
        jsonBuilder.append("\"relativeStopLoss\": ").append(request.getRelativeStopLoss()).append(",");
        jsonBuilder.append("\"relativeTakeProfit\": ").append(request.getRelativeTakeProfit()).append(",");
        Double volumeMultiplier = request.getAccount().getVolumeMultiplier();
        // Tính toán giá trị của volume bằng cách nhân với volumeMultiplier return int
        int volumeSend = (int) Math.round(volumeMultiplier * request.getVolume());
        jsonBuilder.append("\"volume\": ").append(volumeSend);
        jsonBuilder.append("}}");

        return jsonBuilder.toString();
    }

    public CompletableFuture<String> getAccountListByAccessToken() {
        String message = createGetAccountListMessage();
        return sendRequest(message);
    }

    public CompletableFuture<String> authenticateTraderAccount(int ctidTraderAccountId) {
        // Lưu trữ ID tài khoản để sử dụng sau khi kết nối được xác thực
        this.authenticatedTraderAccountId = ctidTraderAccountId;

        // Kiểm tra xem kết nối đã được xác thực ứng dụng chưa
        if (!isApplicationAuthenticated()) {
            log.warn("Cannot authenticate trader account: {} - Application not authenticated yet", ctidTraderAccountId);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.complete("Application not authenticated yet. Will authenticate trader account when ready.");
            return future;
        }
        String message = createAuthenticateTraderAccountMessage(ctidTraderAccountId);
        log.info("Sending authentication request for trader account: {}", ctidTraderAccountId);
        return sendRequest(message);
        // Normally would set up a way to resolve this future when response comes back
        // This is simplified for the example
    }

    private String createClosePositionMessage(String clientMsgId, int positionId, int volume, PayloadType payloadType) {
        // This is a simplified version - in real implementation, use protobuf
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"clientMsgId\": \"").append(clientMsgId).append("\",");
        jsonBuilder.append("\"payloadType\": ").append(payloadType.getValue()).append(",");
//        jsonBuilder.append("\"payloadType\": 2111,");
        jsonBuilder.append("\"payload\": {");
        jsonBuilder.append("\"ctidTraderAccountId\": ").append(authenticatedTraderAccountId).append(",");
        jsonBuilder.append("\"positionId\": ").append(positionId).append(",");
        jsonBuilder.append("\"volume\": ").append(volume);
        jsonBuilder.append("}}");

        return jsonBuilder.toString();
    }

    private String generateClientMsgId() {
        // Generate a unique client message ID for tracking responses
        // Ví dụ: "myPrefix_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.nanoTime();
        return prefix + UUID.randomUUID().toString().substring(0, 6) + "_" + System.nanoTime();
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    public CompletableFuture<String> sendRequest(String message) {
        if (session != null && session.isOpen()) {
            log.info("Sending message in local: {}", message);
            // Kiểm tra xem đây có phải là yêu cầu xác thực ứng dụng không
            boolean isAuthRequest = message.contains("\"payloadType\": 2100");
            if (!isAuthRequest && !connected) {
                log.warn("Cannot send request - Application not authenticated yet: {}", message);
                return CompletableFuture.completedFuture("Application not authenticated yet. Request will be sent when ready.");
            }
            responseFuture = new CompletableFuture<>();
            session.getAsyncRemote().sendText(message);

            return responseFuture;
        } else {
            return CompletableFuture.completedFuture("Connection not available for account: " + accountId);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        log.info("Connected to cTrader for account: " + accountId);
        try {
            sendAuthMessage();
            log.info("Authentication message sent for account: {}", accountId);
        } catch (Exception e) {
            log.error("Failed to send authentication message for account: {}", accountId, e);
        }
//        startPingScheduler();
    }

    public void startPingScheduler() {
        if (pingScheduler == null || pingScheduler.isShutdown()) {
            pingScheduler = Executors.newSingleThreadScheduledExecutor();
            pingScheduler.scheduleAtFixedRate(() -> {
                synchronized (session) {
                    if (session != null && session.isOpen()) {
                        try {
                            // 1. Tạo ProtoHeartbeatEvent
                            String message = createHeartbeatMessage();
                            // 2. Gửi qua WebSocket
                            session.getAsyncRemote().sendText(message);
//                            System.out.println("Sent ProtoHeartbeatEvent ping to cTrader server...");
                            log.info("Sent ProtoHeartbeatEvent ping to cTrader server: Account : {}", accountId);
                        } catch (Exception e) {
                            log.error("Failed to send heartbeat:", e);
//                            System.err.println("Failed to send heartbeat: " + e.getMessage());
                        }
                    }
                }
            }, 0, 15, TimeUnit.SECONDS); // ping mỗi 15 giây
        }
    }

    //create function stop scheduler
    private void stopPingScheduler() {
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdownNow();
            log.info("Stopped ping scheduler for account: {}", accountId);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Received message for account " + accountId + ": " + message);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message);
            //check header beat
            int payloadType = rootNode.get("payloadType").asInt();
            PayloadType payloadTypeEnum = PayloadType.fromValue(payloadType);
            if (payloadTypeEnum == PayloadType.PROTO_OA_HEART_BEAT) {
                log.info("ProtoHeartbeatEvent received for account: {}", accountId);
                return;
            }
            String clientMsgId = rootNode.path("clientMsgId").asText();
//            if (pendingRequests.containsKey(clientMsgId)) {
//                pendingRequests.get(clientMsgId).complete(message);
//                pendingRequests.remove(clientMsgId);
//            } else {
//                log.warn("Received response with unknown clientMsgId: {}", clientMsgId);
//            }
            // Convert dto sang JSON
            // Gửi vào Kafka
            // check prefix clientMsgId la tradebot365 thì gửi vào kafka
            if (clientMsgId.startsWith(prefix)) {
                Map<String, Object> kafkaData = new HashMap<>();
                kafkaData.put("accountId", accountId.toString());
                kafkaData.put("messageType", payloadTypeEnum);
                kafkaData.put("timestamp", System.currentTimeMillis());
                kafkaData.put("rawMessage", message);
                kafkaData.put("clientMsgId", clientMsgId);
                // Xử lý các loại thông báo khác dựa trên payloadType (nếu cần)
                switch (Objects.requireNonNull(payloadTypeEnum)) {
                    case PROTO_OA_CLOSE_POSITION_REQ:
                    case PROTO_OA_EXECUTION_EVENT:
                    case PROTO_OA_ORDER_ERROR_EVENT:
                    case PROTO_OA_ERROR_RES:
                        //check error authen
                        ResponseCtraderDTO res = ValidateRepsone.formatResponse(message);
                        //check actionsystem is order
                        if (actionSystem != null ) {
                            String jsonMessage = objectMapper.writeValueAsString(kafkaData);
                            if (actionSystem.equals(ActionSystem.ORDER)){
                                // xử lý logic khi nhận được message từ websocket

                                log.info("Sending message to topic {}: key={}, value={}", "order-status-topic", clientMsgId, jsonMessage);
                                kafkaTemplate.send("order-status-topic", jsonMessage);
                            } else if (actionSystem.equals(ActionSystem.AUTH)){
                                if (!res.getErrorCode().equals("N/A")){
                                    this.manualDisconnect = true;
                                }
                                kafkaTemplate.send("order-status-auth", jsonMessage);
                                break;
                            }
                        }
                        break;
                    case PROTO_OA_APPLICATION_AUTH_RES:
                        connected = true;
                        log.info("Successfully Application authenticated");
                        if (authenticatedTraderAccountId != 0) {
                            log.info("Auto authenticating trader account: {}", authenticatedTraderAccountId);
                            authenticateTraderAccount(authenticatedTraderAccountId);
                        }
                        connectionService.saveConnectionDetails(this);
                        break;
                    case PROTO_OA_ACCOUNT_AUTH_RES:
                        log.info("Successfully authenticated trader account: {}", authenticatedTraderAccountId);
                        connectionService.saveConnectionAuthenticated(this);
                        startPingScheduler();
                        break;
                    default:
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (responseFuture != null) {
            responseFuture.complete(message); // Hoàn thành Future khi nhận được dữ liệu
            //ghi log message
            log.info("Message Ctrader response: {}", message);
            responseFuture = null; // Reset để dùng cho request tiếp theo
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.warn("Connection closed for account = {} and session ID={}", accountId + ": " + reason.getReasonPhrase(), session.getId());
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            log.error("Error closing session for account " + accountId, e);
        }
        // log clientSessionId
        log.info("Session close by Client Session Id: {}", session.getId());
        // Only reconnect if this wasn't a manual disconnect

        if (!manualDisconnect) {
            connectionService.reconnect(this); // Tự động reconnect khi đóng
        } else {
            log.info("Manual disconnect detected for account: {}, not reconnecting", accountId);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("Error for account " + accountId + ": " + throwable.getMessage());
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            log.error("Error closing session for account " + accountId, e);
        }
        if (!manualDisconnect) {
            connectionService.reconnect(this); // Tự động reconnect khi đóng
        } else {
            log.info("Manual disconnect detected for account: {}, not reconnecting", accountId);
        }
    }

    public void close() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                log.error("Failed to close connection for account " + accountId + ": " + e.getMessage());
            }
        }
    }


}