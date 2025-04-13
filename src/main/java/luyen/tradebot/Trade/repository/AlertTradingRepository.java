package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.AlertTradingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AlertTradingRepository extends JpaRepository<AlertTradingEntity, Long>{
    Page<AlertTradingEntity> findAll(Pageable pageable, String search, String sorts, LocalDateTime createdAt);

}
