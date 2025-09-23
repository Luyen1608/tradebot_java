package luyen.tradebot.Trade.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import lombok.Data;
import luyen.tradebot.Trade.dto.request.PlaceOrderRequest;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;
import luyen.tradebot.Trade.util.ValidateRepsone;
import luyen.tradebot.Trade.util.enumTraderBot.ActionSystem;
import luyen.tradebot.Trade.util.enumTraderBot.PayloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;


@Data
@ClientEndpoint
public class CTraderConnection {
    private UUID accountId;
    private String accessToken;
    private Double volumeMultiplier;

    private KafkaProducerService kafkaProducerService;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ValidateRepsone validateRepsone;
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
    private String clientMsgId = "";
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final Logger heartbeatLogger = LoggerFactory.getLogger("luyen.tradebot.Trade.service.CTraderConnection.HEARTBEAT");
    private Logger accountLogger;
    public CTraderConnection(UUID accountId, String clientId, String secretId, String accessToken,
                             CTraderConnectionService connectionService, String wsUrl,
                             KafkaTemplate<String, String> kafkaTemplate, KafkaProducerService kafkaProducerService,
                             String prefix, Double volumeMultiplier, int ctidTraderAccountId, String clientMsgId) {
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
        this.clientMsgId = clientMsgId;
        if (prefix != null) {
            this.prefix = prefix;
        }
        // Initialize account-specific logger
        this.accountLogger = initializeAccountLogger(accountId);
    }

    private Logger initializeAccountLogger(UUID accountId) {
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            
            // Create a unique logger name for this account
            String loggerName = "CTraderConnection." + accountId.toString();
            ch.qos.logback.classic.Logger logger = context.getLogger(loggerName);
            
            // Create file appender for this account
            FileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender = new FileAppender<>();
            fileAppender.setContext(context);
            fileAppender.setName("AccountFileAppender-" + accountId);
            fileAppender.setFile("logs/" + accountId + ".log");
            
            // Create encoder
            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(context);
            encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [AccountID: %X{accountId}] %logger{36} - %msg%n");
            encoder.start();
            
            fileAppender.setEncoder(encoder);
            fileAppender.start();
            
            // Add appender to logger
            logger.addAppender(fileAppender);
            logger.setAdditive(false); // Don't inherit appenders from root logger
            
            return logger;
        } catch (Exception e) {
            // Fallback to default logger if account-specific logger creation fails
            Logger fallbackLogger = LoggerFactory.getLogger(CTraderConnection.class);
            fallbackLogger.error("Failed to create account-specific logger for {}, using default logger", accountId, e);
            return fallbackLogger;
        }
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, URI.create(wsUrl));
            actionSystem = ActionSystem.AUTH;
            accountLogger.info("WebSocket connection initiated at {}, waiting for onOpen event... - clientMsgId: {} - authenticatedTraderAccountId: {} - wsUrl: {}", accountId, clientMsgId, authenticatedTraderAccountId, wsUrl);
        } catch (Exception e) {
            accountLogger.error("Failed to connect to cTrader WebSocket at {} for account {}", wsUrl, accountId, e);
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
                accountLogger.info("Disconnected from cTrader WebSocket with Account Id: {}", accountId);
            } catch (Exception e) {
                accountLogger.error("Error disconnecting from cTrader WebSocket for account: {}", accountId, e);
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

    public CompletableFuture<String> getOrderList(int ctidTraderAccountId, Long fromTimestamp, Long toTimestamp) {
        String message = createOrderListRequestMessage(ctidTraderAccountId, fromTimestamp, toTimestamp);
        return sendRequest(message);
    }
    public CompletableFuture<String> getDetailList(int ctidTraderAccountId, Long fromTimestamp, Long toTimestamp, int maxRows) {
        String message = createDetailListRequestMessage(ctidTraderAccountId, fromTimestamp, toTimestamp,maxRows);
        return sendRequest(message);
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
        if (request.getRelativeStopLoss() != 0){
        jsonBuilder.append("\"relativeStopLoss\": ").append(request.getRelativeStopLoss()).append(",");}
        if (request.getRelativeTakeProfit() != 0){
        jsonBuilder.append("\"relativeTakeProfit\": ").append(request.getRelativeTakeProfit()).append(",");}

        Double volumeMultiplier = request.getAccount().getVolumeMultiplier();
        // Tính toán giá trị của volume bằng cách nhân với volumeMultiplier return int
        int volumeSend = (int) Math.round(volumeMultiplier * request.getVolume());
        jsonBuilder.append("\"volume\": ").append(volumeSend);
        jsonBuilder.append("}}");

        return jsonBuilder.toString();
    }

    private String createOrderListRequestMessage(int ctidTraderAccountId, Long fromTimestamp, Long toTimestamp) {
        // This is a simplified version - in real implementation, use protobuf
        // JSON format for order placement
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"clientMsgId\": \"").append(generateClientMsgId()).append("\",");
//        jsonBuilder.append("\"payloadType\": 2106,");
        jsonBuilder.append("\"payloadType\": ").append("2175").append(",");
        jsonBuilder.append("\"payload\": {");
        jsonBuilder.append("\"ctidTraderAccountId\": ").append(ctidTraderAccountId).append(",");
        jsonBuilder.append("\"fromTimestamp\": ").append(fromTimestamp).append(",");
        jsonBuilder.append("\"toTimestamp\": ").append(toTimestamp);
        jsonBuilder.append("}}");
//        jsonBuilder.append("}");

        return jsonBuilder.toString();
    }
    private String createDetailListRequestMessage(int ctidTraderAccountId, Long fromTimestamp, Long toTimestamp, int maxRows) {
        // This is a simplified version - in real implementation, use protobuf
        // JSON format for order placement
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"clientMsgId\": \"").append(generateClientMsgId()).append("\",");
//        jsonBuilder.append("\"payloadType\": 2106,");
        jsonBuilder.append("\"payloadType\": ").append("2133").append(",");
        jsonBuilder.append("\"payload\": {");
        jsonBuilder.append("\"ctidTraderAccountId\": ").append(ctidTraderAccountId).append(",");
        jsonBuilder.append("\"fromTimestamp\": ").append(fromTimestamp).append(",");
        jsonBuilder.append("\"toTimestamp\": ").append(toTimestamp).append(",");
        jsonBuilder.append("\"maxRows\": ").append(maxRows);
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
            accountLogger.warn("Cannot authenticate trader account: {} - Application not authenticated yet for account: {}", ctidTraderAccountId, accountId);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.complete("Application not authenticated yet. Will authenticate trader account when ready.");
            return future;
        }
        String message = createAuthenticateTraderAccountMessage(ctidTraderAccountId);
        accountLogger.info("Sending authentication request for trader account: {} for account: {}", ctidTraderAccountId, accountId);
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
            accountLogger.info("Sending message in local for account {}: {}", accountId, message);
            // Kiểm tra xem đây có phải là yêu cầu xác thực ứng dụng không
            boolean isAuthRequest = message.contains("\"payloadType\": 2100");
            if (!isAuthRequest && !connected) {
                accountLogger.warn("Cannot send request - Application not authenticated yet for account {}: {}", accountId, message);
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
        accountLogger.info("Connected to cTrader for account: {}", accountId);
        try {
            sendAuthMessage();
            accountLogger.info("Authentication message sent for account: {}", accountId);
        } catch (Exception e) {
            accountLogger.error("Failed to send authentication message for account: {}", accountId, e);
        }
//        startPingScheduler();
    }

    public void startPingScheduler() {
        if (pingScheduler == null || pingScheduler.isShutdown()) {
            pingScheduler = Executors.newSingleThreadScheduledExecutor();
            pingScheduler = Executors.newScheduledThreadPool(2);
            pingScheduler.scheduleAtFixedRate(() -> {
                synchronized (session) {
                    try {
                        if (session != null && session.isOpen()) {
                            long startTime = System.nanoTime();

                            // 1. Tạo ProtoHeartbeatEvent
                            String message = createHeartbeatMessage();
                            // 2. Gửi qua WebSocket
                            session.getAsyncRemote().sendText(message, result -> {
                                if (!result.isOK()) {
                                    accountLogger.error("Lỗi khi gửi heartbeat: {}", result.getException().getMessage());
                                    connectionService.reconnect(this, "");
                                    try {
                                        Thread.sleep(10000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
    //                            System.out.println("Sent ProtoHeartbeatEvent ping to cTrader server...");
                            accountLogger.info("Sent ProtoHeartbeatEvent ping to cTrader server: Account : {} : {} ms", accountId, (System.nanoTime() - startTime) / 1_000_000);

                        }else {
                            accountLogger.warn("Session không hợp lệ hoặc đã đóng cho tài khoản: {}", accountId);
                            connectionService.reconnect(this,"");
                            Thread.sleep(10000);
                            return;
                        }
                    } catch (Exception e) {
                        accountLogger.error("Failed to send heartbeat:", e);
                        connectionService.reconnect(this, "");
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
//                            System.err.println("Failed to send heartbeat: " + e.getMessage());
                    }
                }
            }, 0, 15, TimeUnit.SECONDS); // ping mỗi 15 giây
        }
    }

    //create function stop scheduler
    private void stopPingScheduler() {
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdownNow();
            accountLogger.info("Stopped ping scheduler for account: {}", accountId);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message);
            //check header beat
            int payloadType = rootNode.get("payloadType").asInt();
            PayloadType payloadTypeEnum = PayloadType.fromValue(payloadType);
            if (payloadTypeEnum == PayloadType.PROTO_OA_HEART_BEAT || payloadTypeEnum == PayloadType.PROTO_MESSAGE
                    || payloadTypeEnum == PayloadType.ERROR_RES) {
                Map<String, Object> kafkaData = new HashMap<>();
                kafkaData.put("accountId", accountId.toString());
                kafkaData.put("messageType", payloadTypeEnum);
                kafkaData.put("rawMessage", message);
                String jsonMessage = objectMapper.writeValueAsString(kafkaData);

                accountLogger.info("ProtoHeartbeatEvent received for account {} : {}",accountId.toString(), message);
                kafkaTemplate.send("account-status", jsonMessage);
//                log.info("ProtoHeartbeatEvent received for account: {}", accountId);
                return;
            }

            String clientMsgId = rootNode.path("clientMsgId").asText();
            accountLogger.info("Received message for account {}: {}", accountId, message);
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
                String jsonMessage = objectMapper.writeValueAsString(kafkaData);
                //check error authen
                ResponseCtraderDTO res = ValidateRepsone.formatResponse(message);
                // Xử lý các loại thông báo khác dựa trên payloadType (nếu cần)

                switch (Objects.requireNonNull(payloadTypeEnum)) {
                    case PROTO_OA_CLOSE_POSITION_REQ:
                    case PROTO_OA_EXECUTION_EVENT:
                    case PROTO_OA_ORDER_ERROR_EVENT:
                    case PROTO_OA_ERROR_RES:
                        //check actionsystem is order
                        if (actionSystem != null) {
                            if (actionSystem.equals(ActionSystem.ORDER)) {
                                // xử lý logic khi nhận được message từ websocket
                                accountLogger.info("Sending message to topic {} for account {}: key={}, value={}", "order-status-topic", accountId, clientMsgId, jsonMessage);
                                kafkaTemplate.send("order-status-topic", jsonMessage);
                                //xu ly truong hop payloadType 2142 INVALID_REQUEST -  Trading account is not authorized :
                                if (!"".equals(res.getErrorCode())) {
                                    if ("INVALID_REQUEST".equals(res.getErrorCode()) && res.getDescription().contains("account is not authorized")) {
                                        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                                            reconnectAttempts++;
                                            this.clientMsgId = clientMsgId;
                                            accountLogger.info("Reconnect if order INVALID_REQUEST round for account {}: attempt {} - authenticatedTraderAccountId: {} - clientMsgId: {}", accountId, reconnectAttempts, authenticatedTraderAccountId, clientMsgId);
                                            connectionService.reconnect(this, clientMsgId);

                                        } else {
                                            this.clientMsgId = "";
                                            accountLogger.info("Max reconnect attempts reached for account: {}", accountId);
                                        }

                                    }
                                } else {
                                    if (res.getPayloadType() == 3){
                                        this.clientMsgId = "";
                                        resetReconnectAttempts();
                                    }
                                }

                            } else if (actionSystem.equals(ActionSystem.AUTH)) {
                                if (!"".equals(res.getErrorCode())) {
                                    this.manualDisconnect = true;
                                }
                                kafkaTemplate.send("order-status-auth", jsonMessage);
                                break;
                            }
                        }
                        break;
                    case PROTO_OA_APPLICATION_AUTH_RES:
                        connected = true;
                        accountLogger.info("Successfully Application authenticated for account {} - clientMsgId: {} - authenticatedTraderAccountId: {}", accountId, this.clientMsgId, authenticatedTraderAccountId);
                        if (authenticatedTraderAccountId != 0) {
                            accountLogger.info("Auto authenticating trader account for account {}: authenticatedTraderAccountId: {}", accountId, authenticatedTraderAccountId);
                            authenticateTraderAccount(authenticatedTraderAccountId);
                        }
                        connectionService.saveConnectionDetails(this);
                        break;
                    case PROTO_OA_ACCOUNT_AUTH_RES:
                        accountLogger.info("Successfully authenticated trader account for account {}: authenticatedTraderAccountId: {} - clientMsgId: {}", accountId, authenticatedTraderAccountId, this.clientMsgId);
                        connectionService.saveConnectionAuthenticated(this);
                        startPingScheduler();
                        break;

                    case PROTO_OA_ORDER_LIST_RES:
                        break;
                    default:
                        kafkaTemplate.send("write-log-error", jsonMessage);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (responseFuture != null) {
            responseFuture.complete(message); // Hoàn thành Future khi nhận được dữ liệu
            responseFuture = null; // Reset để dùng cho request tiếp theo
        }
    }
    public void resetReconnectAttempts() {
        this.reconnectAttempts = 0;
        accountLogger.info("Reconnect attempts reset for account: {}", accountId);
    }
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        accountLogger.warn("Connection closed for account {}: {} - session ID: {}", accountId, reason.getReasonPhrase(), session.getId());
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            accountLogger.error("Error closing session for account {}", accountId, e);
        }
        // log clientSessionId
        accountLogger.info("Session close by Client Session Id: {} for account: {}", session.getId(), accountId);
        // Only reconnect if this wasn't a manual disconnect

        if (!manualDisconnect) {
            connectionService.reconnect(this, ""); // Tự động reconnect khi đóng
        } else {
            accountLogger.info("Manual disconnect detected for account: {}, not reconnecting", accountId);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        accountLogger.error("Error for account {}: {}", accountId, throwable.getMessage(), throwable);
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            accountLogger.error("Error closing session for account {}", accountId, e);
        }
        if (!manualDisconnect) {
            connectionService.reconnect(this, ""); // Tự động reconnect khi đóng
        } else {
            accountLogger.info("Manual disconnect detected for account: {}, not reconnecting", accountId);
        }
    }

    public void close() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                accountLogger.error("Failed to close connection for account {}: {}", accountId, e.getMessage(), e);
            }
        }
    }
}


