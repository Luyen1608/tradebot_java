package luyen.tradebot.Trade.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Service
public class CTraderConnectionManager {
    private static final Logger logger = Logger.getLogger(CTraderConnectionManager.class.getName());
    private final Map<String, CTraderWebSocketClient> connections = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<String> requestAccountList(String accessToken) {
        for (CTraderWebSocketClient client : connections.values()) {
            if (client.getAccessToken().equals(accessToken) && client.isOpen()) {
                CompletableFuture<String> future = new CompletableFuture<>();  // 🔹 Tạo một CompletableFuture
                pendingRequests.put(accessToken, future);  // 🔹 Lưu lại để sau này hoàn thành khi có dữ liệu từ WebSocket
                client.sendGetAccountListRequest();  // 🔹 Gửi request lấy danh sách tài khoản
                return future;  // 🔹 Trả về CompletableFuture ngay lập tức
            }
        }
        return CompletableFuture.completedFuture("⚠ No active WebSocket found for the provided accessToken.");
    }
    public void handleWebSocketResponse(String accessToken, String response) {
        if (pendingRequests.containsKey(accessToken)) {
            pendingRequests.get(accessToken).complete(response);  // 🔹 Hoàn thành CompletableFuture và gửi dữ liệu về API
            pendingRequests.remove(accessToken);  // 🔹 Xóa request khỏi danh sách đang chờ
        }
    }



    public void connect(String accountId, String host, int port, String accessToken) {
        if (connections.containsKey(accountId)) {
            logger.info("🔄 Already connected: " + accountId);
            return;
        }
        try {
            CTraderWebSocketClient client = new CTraderWebSocketClient(accessToken, this);
            client.connectBlocking();
            connections.put(accountId, client);
            logger.info("✅ Connected account: " + accountId);
        } catch (URISyntaxException | InterruptedException e) {
            logger.severe("❌ Error creating WebSocket for account: " + accountId);
        }
    }

//    public String requestAccountList(String accessToken) {
//        for (CTraderWebSocketClient client : connections.values()) {
//            if (client.getAccessToken().equals(accessToken) && client.isOpen()) {
//                return client.sendGetAccountListRequest();
//            }
//        }
//        return "⚠ No active WebSocket found for the provided accessToken.";
//    }
}
