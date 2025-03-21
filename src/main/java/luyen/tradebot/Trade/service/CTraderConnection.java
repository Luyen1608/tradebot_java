package luyen.tradebot.Trade.service;


import jakarta.websocket.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.util.enumTraderBot.OrderType;
import luyen.tradebot.Trade.util.enumTraderBot.Symbol;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class CTraderConnection {
    private final String clientId;
    private final String accessToken;
    private final String wsUrl;

    private Session webSocketSession;
    private boolean connected = false;

    private int authenticatedTraderAccountId;

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            webSocketSession = (Session) container.connectToServer(new CTraderWebSocketEndpoint(),
                    URI.create(wsUrl));

            // Authenticate with access token
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
                generateClientMsgId(), clientId, accessToken
        );
        sendMessage(authMessage);
    }

    public void sendMessage(String message) {
        if (webSocketSession != null && webSocketSession.isOpen()) {
            webSocketSession.getAsyncRemote().sendText(message);
        } else {
            log.error("Cannot send message - WebSocket is not connected");
            throw new IllegalStateException("WebSocket is not connected");
        }
    }

    public CompletableFuture<String> placeOrder(Symbol symbol, TradeSide tradeSide,
                                                BigDecimal volume, OrderType orderType) {
        // Create ProtoOANewOrderReq message
        String orderMessage = createOrderMessage(symbol, tradeSide, volume, orderType);

        CompletableFuture<String> future = new CompletableFuture<>();
        // In real implementation, track message ID and resolve future when response is received

        sendMessage(orderMessage);
        return future;
    }

    public CompletableFuture<String> closePosition(String positionId) {
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
                "{\"clientMsgId\": \"%s\",\"payloadType\": 2102,\"payload\": {\"ctidTraderAccountId\": \"%s\",\"accessToken\": \"%s\"}}",
                generateClientMsgId(), ctidTraderAccountId, accessToken
        );
    }
    private String createOrderMessage(Symbol symbol, TradeSide tradeSide,
                                      BigDecimal volume, OrderType orderType) {
        // This is a simplified version - in real implementation, use protobuf
        // JSON format for order placement
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"clientMsgId\": \"").append(generateClientMsgId()).append("\",");
        jsonBuilder.append("\"payloadType\": 2106,");
        jsonBuilder.append("\"payload\": {");
        jsonBuilder.append("\"ctidTraderAccountId\": \"").append(authenticatedTraderAccountId).append("\",");
        jsonBuilder.append("\"symbol\": \"").append(symbol).append("\",");
        jsonBuilder.append("\"tradeSide\": \"").append(tradeSide).append("\",");
        jsonBuilder.append("\"orderType\": ").append(OrderType.MARKET.getValue()).append(",");
        jsonBuilder.append("\"volume\": ").append(volume);

        jsonBuilder.append("}}");

        return jsonBuilder.toString();
    }
    public CompletableFuture<List<Map<String, Object>>> getAccountListByAccessToken() {
        String message = createGetAccountListMessage();

        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

        sendMessage(message);

        // Normally would set up a way to resolve this future when response comes back
        // This is simplified for the example
        return future;
    }

    public CompletableFuture<Boolean> authenticateTraderAccount(int ctidTraderAccountId) {
        String message = createAuthenticateTraderAccountMessage(ctidTraderAccountId);

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        sendMessage(message);
        this.authenticatedTraderAccountId = ctidTraderAccountId;

        // Normally would set up a way to resolve this future when response comes back
        // This is simplified for the example
        return future;
    }
    private String createClosePositionMessage(String positionId) {
        // This is a simplified version - in real implementation, use protobuf
        return String.format(
                "{\"clientMsgId\": \"%s\",\"payloadType\": 2113,\"payload\": {\"ctidTraderAccountId\": \"%s\",\"positionId\": \"%s\"}}",
                generateClientMsgId(), authenticatedTraderAccountId, positionId
        );
    }
    private String generateClientMsgId() {
        // Generate a unique client message ID for tracking responses
        return "cm_" + UUID.randomUUID().toString().substring(0, 8);
    }


    @ClientEndpoint
    public class CTraderWebSocketEndpoint {
        @OnOpen
        public void onOpen(Session session) {
            log.info("WebSocket connection opened");
        }

        @OnMessage
        public void onMessage(String message) {
            log.debug("Received message: {}", message);
            // Process incoming messages, update message tracking and resolve futures
        }

        @OnClose
        public void onClose(Session session, CloseReason reason) {
            log.info("WebSocket connection closed: {}", reason);
            connected = false;
        }

        @OnError
        public void onError(Session session, Throwable throwable) {
            log.error("WebSocket error", throwable);
        }
    }
    // Helper method to parse JSON responses
    private void parseJsonResponse(String jsonMessage) {
        // In a real implementation, use a JSON library like Jackson to parse responses
        // and resolve the appropriate CompletableFuture based on the requestId
    }
}