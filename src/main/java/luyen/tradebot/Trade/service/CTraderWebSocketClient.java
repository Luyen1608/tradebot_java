package luyen.tradebot.Trade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@Getter
public class CTraderWebSocketClient extends WebSocketClient {
    private static final Logger logger = Logger.getLogger(CTraderWebSocketClient.class.getName());
    private final String accessToken;
    private final CTraderConnectionManager connectionManager;
    private Timer pingTimer;

    private CTraderWebSocketClient(CTraderConnectionManager connectionManager) throws URISyntaxException {
        this(null, connectionManager);
    }

    //
    public CTraderWebSocketClient(String accessToken, CTraderConnectionManager connectionManager) throws URISyntaxException {
//        super(new URI("wss://" + host + ":" + port));
        super(new URI("wss://demo.ctraderapi.com:5036"));
        this.accessToken = accessToken;
        this.connectionManager = connectionManager;
    }



    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("✅ Connected to cTrader WebSocket");
        sendAuthorizationRequest();
    }

    @Override
    public void onMessage(String message) {
        logger.info("✅ Received Account List: " + message);
        connectionManager.handleWebSocketResponse(this.accessToken, message);
//        if (message.contains("\"payloadType\":2150")) {
//            logger.info("✅ Received Account List: " + message);
//            connectionManager.handleWebSocketResponse(this.accessToken, message);
//        } else if (message.contains("errorCode")) {
//            logger.severe("❌ Error from server: " + message);
//        }

//        try {
//            JsonNode jsonNode = new ObjectMapper().readTree(message);
//            int payloadType = jsonNode.get("payloadType").asInt();
//
//            logger.info("✅ Received Ctrader List: " + message);
//            if (payloadType == 2103) { // ProtoOAGetAccountListByAccessTokenRes
//                connectionManager.handleWebSocketResponse(this.accessToken, message);
//            } else if (payloadType == 2150) { // ProtoOAGetAccountListByAccessTokenReq
//                connectionManager.handleWebSocketResponse(this.accessToken, message);
//            } else if (payloadType == 2122) { // ProtoOANewOrderRes
//                connectionManager.handleWebSocketResponse(this.accessToken, message);
//            } else if (payloadType == 2124) { // ProtoOAClosePositionRes
//                connectionManager.handleWebSocketResponse(this.accessToken, message);
//            } else if (jsonNode.has("errorCode")) {
//                logger.severe("❌ Server returned error: " + message);
//            } else {
//                logger.info("🔍 Unknown response received: " + message);
//            }
//        } catch (JsonProcessingException e) {
//            logger.severe("⚠ Error parsing WebSocket message: " + e.getMessage());
//        }



        logger.info("📩 Received: ");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.warning("❌ Connection closed: " + reason);
        stopPing();
        reconnect();
    }
    private void startPing() {
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendPing();
            }
        }, 5000, 15000); // Ping mỗi 15 giây
    }
    private void stopPing() {
        if (pingTimer != null) {
            pingTimer.cancel();
        }
    }

    public void sendPing() {
        send("{ \"payloadType\": \"Ping\" }");
        System.out.println("📤 Sent Ping");
    }

    public void reconnect() {
        System.out.println("🔄 Reconnecting...");
        try {
            Thread.sleep(5000);
            reconnectBlocking(); // Kết nối lại ngay lập tức
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onError(Exception ex) {
        logger.severe("⚠ WebSocket Error: " + ex.getMessage());
    }

    private void sendAuthorizationRequest() {
//        String authRequest = "{ \"payloadType\": \"ProtoOAApplicationAuthReq\", \"clientId\": \"13710_0O0OkCePyvqDVC0ggfQp8Gzc6EWwlEBPkLOcepSVHeVKYXl1LE\", \"clientSecret\": \"U9hXhfBS1mUo6OAW0giE2ulJnIHkBKt85dA19YLPnNsyhF8iNR\" }";
        String authRequest = "{"
                + "\"clientMsgId\": \"cm_id_2\","
                + "\"payloadType\": 2100,"
                + "\"payload\": {"
                + "\"clientId\": \"13710_0O0OkCePyvqDVC0ggfQp8Gzc6EWwlEBPkLOcepSVHeVKYXl1LE\","
                + "\"clientSecret\": \"U9hXhfBS1mUo6OAW0giE2ulJnIHkBKt85dA19YLPnNsyhF8iNR\""
                + "}"
                + "}";
        this.send(authRequest);
        logger.info("📤 Sent: Authorization Request");
    }
    public void sendAccountAuthorizationRequest(String accessToken, int ctidTraderAccountId) {
//        String authRequest = "{ \"payloadType\": \"ProtoOAApplicationAuthReq\", \"clientId\": \"13710_0O0OkCePyvqDVC0ggfQp8Gzc6EWwlEBPkLOcepSVHeVKYXl1LE\", \"clientSecret\": \"U9hXhfBS1mUo6OAW0giE2ulJnIHkBKt85dA19YLPnNsyhF8iNR\" }";
        String accountAuthen = "{"
                + "\"clientMsgId\": \"cm_id_2\","
                + "\"payloadType\": 2102,"
                + "\"payload\": {"
                + "\"ctidTraderAccountId\": " + ctidTraderAccountId + ","
                + "\"accessToken\": \"" + accessToken + "\""
                + "}"
                + "}";
        this.send(accountAuthen);
        logger.info("📤 Sent: Authorization Request");
    }

    public String sendGetAccountListRequest(String accessToken) {
        String request = "{"
                + "\"clientMsgId\": \"cm_id_2\","
                + "\"payloadType\": 2149,"
                + "\"payload\": {"
                + "\"accessToken\": \"" + accessToken + "\""
                + "}"
                + "}";
//        String request = "{ \"payloadType\": \"ProtoOAGetAccountListByAccessTokenReq\", \"accessToken\": \"" + accessToken + "\" }";
        this.send(request);
        logger.info("📤 Sent: ProtoOAGetAccountListByAccessTokenReq");
        return "✅ Request sent for account list.";
    }
    public void sendPlaceOrderRequest(int ctidTraderAccountId, int orderType, double volume, int tradeSide, int symbolId) {
//        String request = String.format(
//                "{ \"payloadType\": 2106, \"payload\": { \"ctidTraderAccountId\": %s, \"orderType\": %d, \"volume\": %f, \"tradeSide\": %s, \"symbolId\": %d } }",
//                ctidTraderAccountId, orderType, volume, tradeSide, symbolId
//        );
//        this.send(request);
//        logger.info("📤 Sent: ProtoOANewOrderReq");


        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> payload = new HashMap<>();
        payload.put("ctidTraderAccountId", ctidTraderAccountId);
        payload.put("orderType", orderType);
        payload.put("symbolId", symbolId);
        payload.put("tradeSide", tradeSide);
        payload.put("volume", (int) (volume * 1000)); // Convert sang micro lot
//        if (openPrice != null) payload.put("openPrice", openPrice);
//        if (stopLoss != null) payload.put("stopLoss", stopLoss);
//        if (takeProfit != null) payload.put("takeProfit", takeProfit);
//        payload.put("timeInForce", timeInForce);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("clientMsgId", "ms_id_3");
        requestMap.put("payloadType", 2106);
        requestMap.put("payload", payload);

        try {
            String requestJson = objectMapper.writeValueAsString(requestMap);
            this.send(requestJson);
            logger.info("📤 Sent ProtoOANewOrderReq: " + requestJson);
        } catch (JsonProcessingException e) {
            logger.severe("⚠ Error creating JSON request: " + e.getMessage());
        }
    }

    public void sendClosePositionRequest(int ctidTraderAccountId, int positionId, double volume) {
        String request = "{"
                + "\"clientMsgId\": \"cm_id_close\","
                + "\"payloadType\": 2111,"
                + "\"payload\": {"
                + "\"ctidTraderAccountId\": " + ctidTraderAccountId + ","
                + "\"positionId\": " + positionId + ","
                + "\"volume\": " + (int) (volume * 1000)
                + "}"
                + "}";
        this.send(request);
        logger.info("📤 Sent: ProtoOAClosePositionReq");
    }

}

