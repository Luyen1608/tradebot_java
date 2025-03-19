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

    private final CTraderConnectionManager connectionManager;

    @PostMapping("/connect")
    public String connect(@RequestParam(required = false) String accountId, @RequestParam String accessToken) {
        connectionManager.connect(accountId, "demo.ctraderapi.com", 5036, accessToken);
        return "✅ Connecting to cTrader WebSocket for account: " + accountId;
    }

    @PostMapping("/request-account-list")
    public CompletableFuture<String> requestAccountList(@RequestParam String accessToken) {
        return connectionManager.requestAccountList(accessToken);
    }

    //    "ctidTraderAccountId":42684044,
//            "isLive":false,
//            "traderLogin":5080177,
    @PostMapping("/account-auth")
    public CompletableFuture<ResponseEntity<String>> accountAuth(
            @RequestParam String accessToken,
            @RequestParam int ctidTraderAccountId
    ) {
        return connectionManager.accountAuth(accessToken, ctidTraderAccountId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(500).body("⚠ Error: " + ex.getMessage()));
    }

    @PostMapping("/place-order")
    public CompletableFuture<ResponseEntity<String>> placeOrder(
            @RequestParam String accessToken,
            @RequestParam int ctidTraderAccountId,
            @RequestParam int orderType,
            @RequestParam double volume,
            @RequestParam int tradeSide,
            @RequestParam int symbolId
    ) {
        return connectionManager.placeOrder(accessToken, ctidTraderAccountId, orderType, volume, tradeSide, symbolId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(500).body("⚠ Error: " + ex.getMessage()));
    }


    @PostMapping("/close-position")
    public CompletableFuture<ResponseEntity<String>> closePosition(
            @RequestParam String accessToken,
            @RequestParam int ctidTraderAccountId,
            @RequestParam int positionId,
            @RequestParam double volume
    ) {
        return connectionManager.closePosition(accessToken, ctidTraderAccountId, positionId, volume)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(500).body("⚠ Error: " + ex.getMessage()));
    }
}
