package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.ConnectedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectRepository extends JpaRepository<ConnectedEntity, Long>{

}
