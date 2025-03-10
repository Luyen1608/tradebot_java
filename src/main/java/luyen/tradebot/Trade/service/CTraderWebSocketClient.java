package luyen.tradebot.Trade.service;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class CTraderWebSocketClient extends WebSocketClient {
    private final String accessToken;
    private final String clientId;
    private final String secret;
    private Timer pingTimer;
//    live.ctraderapi.com:5035 (for operating with Protobufs)	demo.ctraderapi.com:5035 (for operating with Protobufs)
//    live.ctraderapi.com:5036 (for operating with JSON)	demo.ctraderapi.com:5036 (for operating with JSON)
    public CTraderWebSocketClient(String accessToken, String clientId, String secret) throws URISyntaxException {
        super(new URI("wss://demo.ctraderapi.com:5035"));
        this.accessToken = accessToken;
        this.clientId = clientId;
        this.secret = secret;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("✅ Connected: " + accessToken);
        authenticate();
        startPing();
    }

    @Override
    public void onMessage(String message) {
        System.out.println("📩 [" + accessToken + "] Received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("❌ Closed [" + accessToken + "]: " + reason);
        stopPing();
        reconnect();
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("⚠ Error [" + accessToken + "]: " + ex.getMessage());
        reconnect();
    }

    private void authenticate() {
//        "{"clientMsgId": "cm_id_2", "payloadType": 2100, "payload": {"clientId": "34Rsd_T098asHkl","clientSecret": "validClientSecret"}}"
//        clientId 13710_0O0OkCePyvqDVC0ggfQp8Gzc6EWwlEBPkLOcepSVHeVKYXl1LE
//            serect U9hXhfBS1mUo6OAW0giE2ulJnIHkBKt85dA19YLPnNsyhF8iNR
        String authRequest = "{ \"clientMsgId\": \"cm_id_2\"," +
                "\"payloadType\": \"2100\", \"payload\": \"{\"clientId\" : " + clientId + "\",\"clientSecret\" : " + secret + "\" }}";
        send(authRequest);
    }

    private void startPing() {
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendPing();
            }
        }, 5000, 15000);
    }

    private void stopPing() {
        if (pingTimer != null) {
            pingTimer.cancel();
        }
    }

    public void sendPing() {
        send("{ \"payloadType\": \"Ping\" }");
        System.out.println("📤 [" + accessToken + "] Sent Ping");
    }

    public void reconnect() {
        System.out.println("🔄 Reconnecting [" + accessToken + "]...");
        try {
            Thread.sleep(5000);
            reconnectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

