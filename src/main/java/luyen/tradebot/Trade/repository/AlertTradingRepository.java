package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.AlertTradingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AlertTradingRepository extends JpaRepository<AlertTradingEntity, UUID>{
//    Page<AlertTradingEntity> findAll(Pageable pageable, String search, String sorts, LocalDateTime createdAt);

}
