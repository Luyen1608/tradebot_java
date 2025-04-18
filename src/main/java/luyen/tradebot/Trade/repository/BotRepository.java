package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.BotEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface BotRepository extends JpaRepository<BotEntity, UUID> {
    List<BotEntity> findByIsActive(boolean isActive);
    Optional<BotEntity> findBySignalToken(String signalToken);

    Optional<Object> findByBotName(String botName);

}
