package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.BotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotRepository extends JpaRepository<BotEntity, Long> {
}
