package luyen.tradebot.Trade.controller;

import luyen.tradebot.Trade.service.CTraderConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ctrader")
public class TradingController {

    @Autowired
    private CTraderConnectionManager connectionManager;


    @PostMapping("/connect")
    public String connect(@RequestParam String accountId, @RequestParam String accessToken) {
        connectionManager.connect(accountId, "demo.ctraderapi.com", 5036, accessToken);
        return "✅ Connecting to cTrader WebSocket for account: " + accountId;
    }

    @PostMapping("/request-account-list")
    public String requestAccountList(@RequestParam String accessToken) {
        return connectionManager.requestAccountList(accessToken);
    }
}
