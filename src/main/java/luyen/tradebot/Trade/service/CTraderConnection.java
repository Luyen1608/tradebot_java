package luyen.tradebot.Trade.service;


import jakarta.websocket.*;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.util.ValidateRepsone;
import luyen.tradebot.Trade.util.enumTraderBot.OrderType;
import luyen.tradebot.Trade.util.enumTraderBot.Symbol;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Data
@ClientEndpoint
public class CTraderConnection {
    private final Long accountId;
    private final String accessToken;

    @Value("${tradebot.prefix}")
    public static String prefix;

    private String clientId;
    private String secretId;
    private String wsUrl;

    private Session session;
    private final CTraderConnectionService connectionService;

    private Session webSocketSession;
    private boolean connected = false;
    private CompletableFuture<String> responseFuture; // Lưu trữ phản hồi từ WebSocket

    private int authenticatedTraderAccountId;

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
            connected = true;
            log.info("Connected to cTrader WebSocket at {}", wsUrl);
        } catch (Exception e) {
            log.error("Failed to connect to cTrader WebSocket at {}", wsUrl, e);
            throw new RuntimeException("WebSocket connection failed", e);
        }
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
        sendMessage(authMessage);
    }

    public void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        }
    }

    public CompletableFuture<String> placeOrder(Symbol symbol, TradeSide tradeSide,
                                                BigDecimal volume, OrderType orderType) {
        // Create ProtoOANewOrderReq message
        String orderMessage = createOrderMessage(symbol, tradeSide, volume, orderType);

        // In real implementation, track message ID and resolve future when response is received

//        sendMessage(orderMessage);
//        return future;
        return sendRequest(orderMessage);
    }

    public CompletableFuture<String> closePosition(Integer positionId) {
        // Create ProtoOAClosePositionReq message
        String closeMessage = createClosePositionMessage(positionId);

        CompletableFuture<String> future = new CompletableFuture<>();
        // In real implementation, track message ID and resolve future when response is received

        sendMessage(closeMessage);
        return future;
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

    private String createOrderMessage(Symbol symbol, TradeSide tradeSide,
                                      BigDecimal volume, OrderType orderType) {
        // This is a simplified version - in real implementation, use protobuf
        // JSON format for order placement
       // thực hiện nhân volume với 1000 và convert sang kiểu dữ liệu là integer
        int volumeInt = volume.multiply(BigDecimal.valueOf(1000)).intValue();
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"clientMsgId\": \"").append(generateClientMsgId()).append("\",");
        jsonBuilder.append("\"payloadType\": 2106,");
        jsonBuilder.append("\"payload\": {");
        jsonBuilder.append("\"ctidTraderAccountId\": ").append(authenticatedTraderAccountId).append(",");
        jsonBuilder.append("\"symbolId\": ").append(symbol.getId()).append(",");
        jsonBuilder.append("\"tradeSide\": ").append(tradeSide.getValue()).append(",");
        jsonBuilder.append("\"orderType\": ").append(orderType.getValue()).append(",");
        jsonBuilder.append("\"volume\": ").append(volumeInt);

        jsonBuilder.append("}}");

        return jsonBuilder.toString();
    }

    public CompletableFuture<String> getAccountListByAccessToken() {
        String message = createGetAccountListMessage();
        return sendRequest(message);
    }

    public CompletableFuture<String> authenticateTraderAccount(int ctidTraderAccountId) {
        String message = createAuthenticateTraderAccountMessage(ctidTraderAccountId);

        this.authenticatedTraderAccountId = ctidTraderAccountId;
        log.info("Authenticated for account: {}", ctidTraderAccountId);
        return sendRequest(message);
        // Normally would set up a way to resolve this future when response comes back
        // This is simplified for the example
    }

    private String createClosePositionMessage(Integer positionId) {
        // This is a simplified version - in real implementation, use protobuf
        return String.format(
                "{\"clientMsgId\": \"%s\",\"payloadType\": 2113,\"payload\": {\"ctidTraderAccountId\": \"%s\",\"positionId\": \"%d\"}}",
                generateClientMsgId(), authenticatedTraderAccountId, positionId
        );
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
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Received message for account " + accountId + ": " + message);
        if (responseFuture != null) {
            responseFuture.complete(message); // Hoàn thành Future khi nhận được dữ liệu
            responseFuture = null; // Reset để dùng cho request tiếp theo
        }
        // Xử lý dữ liệu từ cTrader (giá, lệnh, v.v.)
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.warn("Connection closed for account " + accountId + ": " + reason.getReasonPhrase());
        connectionService.reconnect(this); // Tự động reconnect khi đóng
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("Error for account " + accountId + ": " + throwable.getMessage());
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
}