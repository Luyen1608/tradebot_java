package luyen.tradebot.Trade.controller.tradeBot;

import luyen.tradebot.Trade.model.ConnectedEntity;
import luyen.tradebot.Trade.service.CTraderApiService;
import luyen.tradebot.Trade.service.CTraderConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ctrader")
public class CTraderController {

    @Autowired
    private CTraderConnectionService connectionService;

    @Autowired
    private CTraderApiService apiService;

    @GetMapping("/account/{accountId}/price")
    public CompletableFuture<String> getPrice(@PathVariable String accountId) {
        String requestMessage = "{\"payloadType\": \"MARKET_DATA\", \"payload\": {\"symbol\": \"EURUSD\"}}";
//        return connectionService.getDataFromCTrader(accountId, requestMessage)
//                .exceptionally(throwable -> "Error: " + throwable.getMessage());
        return null;
    }

    @GetMapping("/start-connections")
    public String startConnections() {
//        Map<String, String> credentials = apiService.getAllAccountCredentials();
//        connectionService.startConnections(credentials);
        return "Connections started";
    }

    // API mới: Lấy danh sách kết nối hiện tại
    @GetMapping("/connections")
    public List<ConnectedEntity> getConnections() {
        return connectionService.getCurrentConnections();
    }
}