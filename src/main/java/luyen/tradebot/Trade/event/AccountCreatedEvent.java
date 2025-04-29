package luyen.tradebot.Trade.event;

import luyen.tradebot.Trade.service.CTraderConnectionService;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

public class AccountCreatedEvent {
    private final UUID accountId;

    public AccountCreatedEvent(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }


}