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
        connectionManager.connectAll(accessTokens);
    }
}
