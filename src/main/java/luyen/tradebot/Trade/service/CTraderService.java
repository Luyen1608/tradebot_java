package luyen.tradebot.Trade.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public class CTraderService {
    private final CTraderWebSocketClient webSocketClient;

    public CTraderService(CTraderWebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    // Mở kết nối WebSocket
    public void connect() {
        webSocketClient.connect();
    }

    // Đặt lệnh Market Order
//    public void placeMarketOrder(int accountId, int symbolId, int volume) {
//        webSocketClient.placeOrder(accountId, symbolId, volume);
//    }

    // Đóng lệnh
//    public void closeOrder(int accountId, int positionId) {
//        webSocketClient.closeOrder(accountId, positionId);
//    }

    // Đóng kết nối WebSocket
    public void disconnect() {
        webSocketClient.close();
    }
}
