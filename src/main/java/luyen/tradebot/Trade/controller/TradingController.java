package luyen.tradebot.Trade.controller;

import lombok.AllArgsConstructor;
import luyen.tradebot.Trade.service.CTraderConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/ctrader")
@AllArgsConstructor
public class TradingController {

    @Autowired
    private final CTraderConnectionManager connectionManager;


    @PostMapping("/connect")
    public String connect(@RequestParam String accountId, @RequestParam String accessToken) {
        connectionManager.connect(accountId, "demo.ctraderapi.com", 5036, accessToken);
        return "✅ Connecting to cTrader WebSocket for account: " + accountId;
    }

    @PostMapping("/request-account-list")
    public CompletableFuture<String> requestAccountList(@RequestParam String accessToken) {
        return connectionManager.requestAccountList(accessToken);
//        return connectionManager.requestAccountList(accessToken)  // 🔹 Gọi request và chờ phản hồi từ WebSocket
//                .thenApply(response -> ResponseEntity.ok(response))  // 🔹 Khi có dữ liệu, trả về ResponseEntity 200 OK
//                .exceptionally(ex -> ResponseEntity.status(500).body("⚠ Error: " + ex.getMessage()));  // 🔹 Nếu có lỗi, trả về HTTP 500
    }
}
