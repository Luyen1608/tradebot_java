package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.BotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    List<AccountEntity> findByIsActive(boolean isActive);
    List<AccountEntity> findByBotId(Long botId);

    @Query("SELECT a FROM Account a WHERE a.isActive = true AND a.isConnected = false")
    List<AccountEntity> findActiveDisconnectedAccounts();

    @Query("SELECT a FROM Account a WHERE a.bot.id = ?1 AND a.isActive = true AND a.authenticated = true")
    List<AccountEntity> findByBotIdAndIsActiveAndIsAuthenticated(Long botId, boolean isActive, boolean isAuthenticated);
}
