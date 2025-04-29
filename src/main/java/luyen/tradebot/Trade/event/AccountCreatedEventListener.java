package luyen.tradebot.Trade.event;

import luyen.tradebot.Trade.service.CTraderConnectionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AccountCreatedEventListener {

    private final CTraderConnectionService connectionService;

    public AccountCreatedEventListener(CTraderConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAccountCreated(AccountCreatedEvent event) {
        connectionService.connectAccount(event.getAccountId());
    }
}