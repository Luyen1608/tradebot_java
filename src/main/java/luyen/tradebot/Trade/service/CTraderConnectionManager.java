package luyen.tradebot.Trade.service;
import org.java_websocket.client.WebSocketClient;
import org.springframework.stereotype.Service;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CTraderConnectionManager {
    private final Map<String, CTraderWebSocketClient> connections = new ConcurrentHashMap<>();

    public void connectAll(String[] accessTokens) {
        for (String token : accessTokens) {
            connect(token);
        }
    }

    public void connect(String accessToken) {
        if (connections.containsKey(accessToken)) {
            System.out.println("🔄 Already connected: " + accessToken);
            return;
        }
        try {
            CTraderWebSocketClient client = new CTraderWebSocketClient(accessToken);
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
