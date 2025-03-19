package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.AlertTradingEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertTradingRepository extends JpaRepository<AlertTradingEntity, Long>{

}
