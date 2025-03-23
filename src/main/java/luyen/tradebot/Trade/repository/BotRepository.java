package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.BotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BotRepository extends JpaRepository<BotEntity, Long> {
    List<BotEntity> findByIsActive(boolean isActive);
    Optional<BotEntity> findBySignalToken(String signalToken);

    Optional<Object> findByBotName(String botName);
}
