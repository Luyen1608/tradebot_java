package luyen.tradebot.Trade.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CTraderStartupRunner implements CommandLineRunner {
    private final CTraderConnectionManager connectionManager;

    public CTraderStartupRunner(CTraderConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void run(String... args) {
        String[] accessTokens = {
                "-EhgUJQlzyY5HoqH8bsW025hwyRdx0-l1W-9hwuAfU4" // refresh AjbKN7vnygerNmd-vvnYMKqorqup0nw3r8jKEzpIl2U
        };
        String[] clientIds = {
                "13710_0O0OkCePyvqDVC0ggfQp8Gzc6EWwlEBPkLOcepSVHeVKYXl1LE" // refresh AjbKN7vnygerNmd-vvnYMKqorqup0nw3r8jKEzpIl2U
        };
        String[] secrets = {
                "U9hXhfBS1mUo6OAW0giE2ulJnIHkBKt85dA19YLPnNsyhF8iNR" // refresh AjbKN7vnygerNmd-vvnYMKqorqup0nw3r8jKEzpIl2U
        };
        connectionManager.connectAll(accessTokens, clientIds, secrets) ;
    }
}
