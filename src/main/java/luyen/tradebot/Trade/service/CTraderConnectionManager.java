package luyen.tradebot.Trade.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
                client.sendGetAccountListRequest(accessToken);  // 🔹 Gửi request lấy danh sách tài khoản
                return future;  // 🔹 Trả về CompletableFuture ngay lập tức
            }
        }
        return CompletableFuture.completedFuture("⚠ No active WebSocket found for the provided accessToken.");
    }
    public CompletableFuture<String> accountAuth(String accessToken, int ctidTraderAccountId) {
        for (CTraderWebSocketClient client : connections.values()) {
            if (client.getAccessToken().equals(accessToken) && client.isOpen()) {
                CompletableFuture<String> future = new CompletableFuture<>();
//                pendingRequests.put(accessToken + "_acc_auth", future);
                pendingRequests.put(accessToken, future);
                client.sendAccountAuthorizationRequest(accessToken, ctidTraderAccountId);
                return future;
            }
        }
        return CompletableFuture.completedFuture("⚠ No active WebSocket found for the provided accessToken.");
    }
    public CompletableFuture<String> placeOrder(String accessToken, int ctidTraderAccountId, int orderType, double volume, int tradeSide, int symbolId) {
        for (CTraderWebSocketClient client : connections.values()) {
            if (client.getAccessToken().equals(accessToken) && client.isOpen()) {
                CompletableFuture<String> future = new CompletableFuture<>();
//                pendingRequests.put(accessToken + "_new_order", future);
                pendingRequests.put(accessToken, future);
                client.sendPlaceOrderRequest(ctidTraderAccountId, orderType, volume, tradeSide, symbolId);
                return future;
            }
        }
        return CompletableFuture.completedFuture("⚠ No active WebSocket found for the provided accessToken.");
    }


    public CompletableFuture<String> closePosition(String accessToken,int ctidTraderAccountId, int positionId, double volume) {
        for (CTraderWebSocketClient client : connections.values()) {
            if (client.getAccessToken().equals(accessToken) && client.isOpen()) {
                CompletableFuture<String> future = new CompletableFuture<>();
//                pendingRequests.put(accessToken + "_close_position", future);
                pendingRequests.put(accessToken, future);
                client.sendClosePositionRequest(ctidTraderAccountId,positionId, volume);
                return future;
            }
        }
        return CompletableFuture.completedFuture("⚠ No active WebSocket found for the provided accessToken.");
    }

    public void handleWebSocketResponse(String accessToken, String response) {
//        if (pendingRequests.containsKey(accessToken + "_new_order")) {
//            pendingRequests.get(accessToken + "_new_order").complete(response);
//            pendingRequests.remove(accessToken + "_new_order");
//        } else if (pendingRequests.containsKey(accessToken + "_close_position")) {
//            pendingRequests.get(accessToken + "_close_position").complete(response);
//            pendingRequests.remove(accessToken + "_close_position");
//        }


        if (pendingRequests.containsKey(accessToken)) {
            pendingRequests.get(accessToken).complete(response);  // 🔹 Hoàn thành CompletableFuture và gửi dữ liệu về API
            pendingRequests.remove(accessToken);  // 🔹 Xóa request khỏi danh sách đang chờ
        }
    }


    public void connect(String accountId, String host, int port, String accessToken) {
        if (StringUtils.hasText(accountId)) {
            if (connections.containsKey(accountId)) {
                logger.info("🔄 Already connected: " + accountId);
                return;
            }
        }
        try {
            CTraderWebSocketClient client = new CTraderWebSocketClient(accessToken, this);
            client.connectBlocking();
            connections.put(accessToken, client);
            logger.info("✅ Connected account: success" + accountId);
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
