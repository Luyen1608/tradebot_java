package luyen.tradebot.Trade.repository.impl;

import luyen.tradebot.Trade.model.ErrorLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ErrorLogRepository  extends JpaRepository<ErrorLogEntity, UUID> {

}
