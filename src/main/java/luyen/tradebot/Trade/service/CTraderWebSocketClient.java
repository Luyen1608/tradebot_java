package luyen.tradebot.Trade.service;

import lombok.Getter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

@Getter
public class CTraderWebSocketClient extends WebSocketClient {
    private static final Logger logger = Logger.getLogger(CTraderWebSocketClient.class.getName());
    private final String accessToken;
    private final CTraderConnectionManager connectionManager;

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
        if (message.contains("\"payloadType\":2150")) {
            logger.info("✅ Received Account List: " + message);
            connectionManager.handleWebSocketResponse(this.accessToken, message);
        } else if (message.contains("errorCode")) {
            logger.severe("❌ Error from server: " + message);
        }
        logger.info("📩 Received: ");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.warning("❌ Connection closed: " + reason);
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

    public String sendGetAccountListRequest() {
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

}

