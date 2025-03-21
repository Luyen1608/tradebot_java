package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByAccountId(Long accountId);

    List<OrderEntity> findByStatus(String status);

    @Query("SELECT o FROM Order o JOIN o.account a WHERE a.bot.id = ?1 AND o.symbolId = ?2 AND o.status = 'OPEN'")
    List<OrderEntity> findOpenOrdersByBotIdAndSymbolId(Long botId, Integer symbolId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.positions p WHERE o.symbolId = ?1 AND p.status = 'OPEN' AND o.account.bot.signalToken = ?2")
    List<OrderEntity> findOpenOrdersBySymbolIdAndBotSignalToken(Integer symbolId, String signalToken);
}