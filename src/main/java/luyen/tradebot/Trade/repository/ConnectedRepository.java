package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.BotEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConnectedRepository extends JpaRepository<ConnectedEntity, Long> {
    ConnectedEntity findByAccountId(Long accountId);

}
