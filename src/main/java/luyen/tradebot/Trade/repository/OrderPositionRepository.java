package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.OrderPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderPositionRepository extends JpaRepository<OrderPosition, Long> {
    List<OrderPosition> findByOrderId(Long orderId);
    List<OrderPosition> findByAccountId(Long accountId);

    @Query("SELECT op FROM OrderPosition op WHERE op.order.symbolId = ?1 AND op.account.id = ?2 AND op.status = 'OPEN'")
    List<OrderPosition> findOpenPositionsBySymbolIdAndAccountId(Integer symbolId, Long accountId);

    @Query("SELECT op FROM OrderPosition op WHERE op.order.id = ?1 AND op.account.id = ?2")
    Optional<OrderPosition> findByOrderIdAndAccountId(Long orderId, Long accountId);
}