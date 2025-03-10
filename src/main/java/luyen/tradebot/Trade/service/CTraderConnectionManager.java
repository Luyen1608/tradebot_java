package luyen.tradebot.Trade.service;
import org.java_websocket.client.WebSocketClient;
import org.springframework.stereotype.Service;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CTraderConnectionManager {
    private final Map<String, CTraderWebSocketClient> connections = new ConcurrentHashMap<>();

    public void connectAll(String[] accessTokens, String[] clientIds, String[] secrets) {
            connect("-EhgUJQlzyY5HoqH8bsW025hwyRdx0-l1W-9hwuAfU4", "13710_0O0OkCePyvqDVC0ggfQp8Gzc6EWwlEBPkLOcepSVHeVKYXl1LE",
                    "U9hXhfBS1mUo6OAW0giE2ulJnIHkBKt85dA19YLPnNsyhF8iNR");
    }

    public void connect(String accessToken,String clientId, String secret) {
        if (connections.containsKey(accessToken)) {
            System.out.println("🔄 Already connected: " + accessToken);
            return;
        }
        try {
            CTraderWebSocketClient client = new CTraderWebSocketClient(accessToken,clientId,secret);
            client.connect();
            connections.put(accessToken, client);
        } catch (URISyntaxException e) {
            System.err.println("❌ Error creating WebSocket for: " + accessToken);
        }
    }

    public void disconnectAll() {
        connections.values().forEach(WebSocketClient::close);
        connections.clear();
    }
}
