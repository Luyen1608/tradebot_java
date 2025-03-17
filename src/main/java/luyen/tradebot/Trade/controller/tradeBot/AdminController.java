package luyen.tradebot.Trade.controller.tradeBot;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import luyen.tradebot.Trade.dto.request.BotRequestDTO;
import luyen.tradebot.Trade.service.BotService;
import luyen.tradebot.Trade.service.CTraderConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final CTraderConnectionManager connectionManager;

    private final BotService botService;

    @PostMapping("/bot")
    public String addBot(@Valid @RequestBody BotRequestDTO request) {
       long botId =  botService.saveBot(request);
        return "✅ Connecting to cTrader WebSocket for account: " + botId;
    }

    @PostMapping("/request-account-list")
    public CompletableFuture<String> requestAccountList(@RequestParam String accessToken) {
        return connectionManager.requestAccountList(accessToken);
//        return connectionManager.requestAccountList(accessToken)  // 🔹 Gọi request và chờ phản hồi từ WebSocket
//                .thenApply(response -> ResponseEntity.ok(response))  // 🔹 Khi có dữ liệu, trả về ResponseEntity 200 OK
//                .exceptionally(ex -> ResponseEntity.status(500).body("⚠ Error: " + ex.getMessage()));  // 🔹 Nếu có lỗi, trả về HTTP 500
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
        return connectionManager.closePosition(accessToken,ctidTraderAccountId, positionId, volume)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(500).body("⚠ Error: " + ex.getMessage()));
    }
}
