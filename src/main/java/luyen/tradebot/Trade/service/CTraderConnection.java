package luyen.tradebot.Trade.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.repository.OrderPositionRepository;
import luyen.tradebot.Trade.repository.OrderRepository;
import luyen.tradebot.Trade.util.enumTraderBot.ErrorCode;
import luyen.tradebot.Trade.util.enumTraderBot.PayloadType;
import luyen.tradebot.Trade.util.enumTraderBot.ProtoOAExecutionType;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Data
@ClientEndpoint
public class CTraderConnection {
    private Long accountId;
    private String accessToken;

    @Value("${tradebot.prefix}")
    private String prefix = "trade365_";
    private ScheduledExecutorService pingScheduler;
    private String clientId;
    private String secretId;
    private String wsUrl;
    private Session session;
    private CTraderConnectionService connectionService;
    private Session webSocketSession;
    private boolean connected = false;
    private CompletableFuture<String> responseFuture; // Lưu trữ phản hồi từ WebSocket
    private int authenticatedTraderAccountId;
    private String accountType;
    private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();


    public CTraderConnection(Long accountId, String clientId, String secretId, String accessToken, CTraderConnectionService connectionService, String wsUrl) {
        this.accountId = accountId;
        this.accessToken = accessToken;
        this.connectionService = connectionService;
        this.wsUrl = wsUrl;
        this.clientId = clientId;
        this.secretId = secretId;
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, URI.create(wsUrl));
            sendAuthMessage();
            log.info("WebSocket connection established at {}, waiting for authentication...", wsUrl);
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
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close();
                connected = false;
                log.info("Disconnected from cTrader WebSocket");
            } catch (Exception e) {
                log.error("Error disconnecting from cTrader WebSocket", e);
            }
        }
    }

    private void sendAuthMessage() {
        // Implement authentication with access token
        String authMessage = String.format(
                "{\"clientMsgId\": \"%s\",\"payloadType\": 2100,\"payload\": {\"clientId\": \"%s\",\"clientSecret\": \"%s\"}}",
                generateClientMsgId(), clientId, secretId
        );
        sendRequest(authMessage);
    }

    public CompletableFuture<String> placeOrder(int symbol, int tradeSide,
                                                int volume, int orderType) {
        String clientMsgId = generateClientMsgId();
        String orderMessage = createOrderMessage(symbol, tradeSide, volume, orderType, clientMsgId);

        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(clientMsgId, future);
        sendRequest(orderMessage);
        return future;
        // Create ProtoOANewOrderReq message
//        String orderMessage = createOrderMessage(symbol, tradeSide, volume, orderType);

        // In real implementation, track message ID and resolve future when response is received

//        sendRequest(orderMessage);
//        return future;
    }

    public CompletableFuture<String> closePosition(int ctidTraderAccountId, int positionId, int volume) {
        // Create ProtoOAClosePositionReq message
        String closeMessage = createClosePositionMessage(ctidTraderAccountId, positionId, volume);

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

    private String createOrderMessage(int symbol, int tradeSide,
                                      int volume, int orderType, String clientMsgId) {
        // This is a simplified version - in real implementation, use protobuf
        // JSON format for order placement
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"clientMsgId\": \"").append(clientMsgId).append("\",");
        jsonBuilder.append("\"payloadType\": 2106,");
        jsonBuilder.append("\"payload\": {");
        jsonBuilder.append("\"ctidTraderAccountId\": ").append(authenticatedTraderAccountId).append(",");
        jsonBuilder.append("\"symbolId\": ").append(symbol).append(",");
        jsonBuilder.append("\"tradeSide\": ").append(tradeSide).append(",");
        jsonBuilder.append("\"orderType\": ").append(orderType).append(",");
        jsonBuilder.append("\"volume\": ").append(volume);

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

    private String createClosePositionMessage(int ctidTraderAccountId, int positionId, int volume) {
        // This is a simplified version - in real implementation, use protobuf
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"clientMsgId\": \"").append(generateClientMsgId()).append("\",");
        jsonBuilder.append("\"payloadType\": 2111,");
        jsonBuilder.append("\"payload\": {");
        jsonBuilder.append("\"ctidTraderAccountId\": ").append(authenticatedTraderAccountId).append(",");
        jsonBuilder.append("\"positionId\": ").append(positionId).append(",");
        jsonBuilder.append("\"volume\": ").append(volume);
        jsonBuilder.append("}}");

        return jsonBuilder.toString();
    }

    private String generateClientMsgId() {
        // Generate a unique client message ID for tracking responses
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    public CompletableFuture<String> sendRequest(String message) {
        if (session != null && session.isOpen()) {
            // Kiểm tra xem đây có phải là yêu cầu xác thực ứng dụng không
            boolean isAuthRequest = message.contains("\"payloadType\": 2100");
            if (!isAuthRequest && !connected) {
                log.warn("Cannot send request - Application not authenticated yet: {}", message);
                return CompletableFuture.completedFuture("Application not authenticated yet. Request will be sent when ready.");
            }
            responseFuture = new CompletableFuture<>();
            session.getAsyncRemote().sendText(message);
            log.info("Sending message in local: {}", message);
            return responseFuture;
        } else {
            return CompletableFuture.completedFuture("Connection not available for account: " + accountId);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        log.info("Connected to cTrader for account: " + accountId);
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
                            System.out.println("Sent ProtoHeartbeatEvent ping to cTrader server...");
                        } catch (Exception e) {
                            System.err.println("Failed to send heartbeat: " + e.getMessage());
                        }
                    }
                }
            }, 0, 15, TimeUnit.SECONDS); // ping mỗi 15 giây
        }
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Received message for account " + accountId + ": " + message);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message);
            String clientMsgId = rootNode.path("clientMsgId").asText();
            if (pendingRequests.containsKey(clientMsgId)) {
                pendingRequests.get(clientMsgId).complete(message);
                pendingRequests.remove(clientMsgId);
            } else {
                log.warn("Received response with unknown clientMsgId: {}", clientMsgId);
            }
            int payloadType = rootNode.get("payloadType").asInt();
            PayloadType payloadTypeEnum = PayloadType.fromValue(payloadType);
            switch (Objects.requireNonNull(payloadTypeEnum)) {
                case PROTO_OA_EXECUTION_EVENT:
                    processOrderExecutionResponse(rootNode);
                    break;

                case PROTO_OA_ORDER_ERROR_EVENT:
                case PROTO_OA_ERROR_RES:
                    processOrderErrorEvent(rootNode);
                    break;
                case PROTO_OA_APPLICATION_AUTH_RES:
                    connected = true;
                    log.info("Successfully Application authenticated");
                    if (authenticatedTraderAccountId != 0) {
                        log.info("Auto authenticating trader account: {}", authenticatedTraderAccountId);
                        authenticateTraderAccount(authenticatedTraderAccountId);
                    }
                    break;
                case PROTO_OA_ACCOUNT_AUTH_RES:
                    log.info("Successfully authenticated trader account: {}", authenticatedTraderAccountId);
                    break;
                case PROTO_OA_HEART_BEAT:
                    log.info("Account Heart Beat Event Normal: {}", accountId);
                    break;
                default:
//                    log.info("Unknown payload type: {}", payloadType);
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
        // lấy ra executionType trong đoạn này để kiểm tra : {"payloadType":2126,"clientMsgId":"trade365_e64af3df","payload":{"ctidTraderAccountId":42683965,"executionType":3,"position":{"positionId":9880523,"tradeData":{"symbolId":433,"volume":10,"tradeSide":1,"openTimestamp":1743305982603,"guaranteedStopLoss":false,"measurementUnits":"ADA"},"positionStatus":1,"swap":0,"price":0.68123,"utcLastUpdateTimestamp":1743305982603,"commission":0,"marginRate":0.67967,"mirroringCommission":0,"guaranteedStopLoss":false,"usedMargin":1,"moneyDigits":2},"order":{"orderId":16481737,"tradeData":{"symbolId":433,"volume":10,"tradeSide":1,"openTimestamp":1743305982325,"guaranteedStopLoss":false,"measurementUnits":"ADA","closeTimestamp":1743305982603},"orderType":1,"orderStatus":2,"executionPrice":0.68123,"executedVolume":10,"utcLastUpdateTimestamp":1743305982603,"closingOrder":false,"clientOrderId":"trade365_e64af3df","timeInForce":3,"positionId":9880523},"deal":{"dealId":15516749,"orderId":16481737,"positionId":9880523,"volume":10,"filledVolume":10,"symbolId":433,"createTimestamp":1743305982325,"executionTimestamp":1743305982603,"utcLastUpdateTimestamp":1743305982603,"executionPrice":0.68123,"tradeSide":1,"dealStatus":2,"marginRate":0.67967,"commission":0,"baseToUsdConversionRate":0.67967,"moneyDigits":2},"isServerEvent":false}}
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
        connectionService.reconnect(this); // Tự động reconnect khi đóng

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
        connectionService.reconnect(this); // Reconnect khi có lỗi
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

    private void processOrderExecutionResponse(JsonNode rootNode) {
        int executionType = rootNode.path("payload").path("executionType").asInt();
        String clientMsgId = rootNode.path("clientMsgId").asText();
        int positionId = rootNode.path("payload").path("position").path("positionId").asInt();
        String orderStatus = ProtoOAExecutionType.fromCode(executionType).getStatus();
        String errorCode = rootNode.path("payload").has("errorCode") ?
                rootNode.path("payload").get("errorCode").asText(null) : null;
        //update order_postion theo orderId và positionId
        OrderPositionRepository orderPositionRepository = SpringContextHolder.getBean(OrderPositionRepository.class);
        orderPositionRepository.updateByOrderCtraderIdAndPositionId(
                ProtoOAExecutionType.fromCode(executionType).getDescription(),
                errorCode != null ? ErrorCode.fromName(errorCode).getDescription() : null,
                errorCode, orderStatus, clientMsgId);
    }

    private void processOrderErrorEvent(JsonNode rootNode) {
        /*{
           "payloadType":2132,
           "clientMsgId":"trade365_40ce75b8",
           "payload":{
              "errorCode":"TRADING_BAD_VOLUME",
              "ctidTraderAccountId":42684029,
              "description":"Order volume = 0.00 is smaller than minimum allowed volume = 0.01."
           }
        }*/
        String clientMsgId = rootNode.path("clientMsgId").asText();
        String orderStatus = ProtoOAExecutionType.ORDER_REJECTED.getStatus();
        String errorCode = rootNode.path("payload").has("errorCode") ?
                rootNode.path("payload").get("errorCode").asText(null) : null;
        String descriptionError = rootNode.path("payload").has("description") ?
                rootNode.path("payload").get("description").asText(null) : null;
        OrderPositionRepository orderPositionRepository = SpringContextHolder.getBean(OrderPositionRepository.class);
        orderPositionRepository.updateErrorCodeAndErrorMessageByClientMsgId(
                errorCode,
                descriptionError != null ? descriptionError : errorCode != null ? ErrorCode.fromName(errorCode).getDescription() : null,
                orderStatus,
                clientMsgId);
//        OrderRepository orderRepository = SpringContextHolder.getBean(OrderRepository.class);
//        orderRepository.updateStatusById(
//                ProtoOAExecutionType.ORDER_REJECTED.getStatus(), );


    }

}