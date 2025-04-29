package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.BotEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectedRepository extends JpaRepository<ConnectedEntity, UUID> {
//    Optional<ConnectedEntity> findByAccountId(UUID accountId);

}
