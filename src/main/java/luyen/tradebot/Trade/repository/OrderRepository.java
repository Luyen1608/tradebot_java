package luyen.tradebot.Trade.repository;

import jakarta.transaction.Transactional;
import luyen.tradebot.Trade.model.OrderEntity;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByAccountId(Long accountId);

    List<OrderEntity> findByStatus(String status);

    @Query("SELECT o FROM Order o JOIN o.account a WHERE a.bot.id = ?1 AND o.symbolId = ?2 AND o.status = 'OPEN'")
    List<OrderEntity> findOpenOrdersByBotIdAndSymbolId(Long botId, Integer symbolId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.positions p WHERE o.symbolId = ?1 AND o.tradeSide = ?2 AND p.status = 'OPEN' AND o.account.bot.signalToken = ?3")
    List<OrderEntity> findOpenOrdersBySymbolIdAndBotSignalTokenAndTradeSide(Integer symbolId, TradeSide tradeSide, String signalToken);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :status " +
            " WHERE o.id = :id")
    int updateStatusById(@Param("status") String status,
                                                         @Param("id") Long id);
    //lấy row đàu tiên by bot id and status and symbolId
    @Query("SELECT o FROM Order o WHERE o.botId = ?1 AND o.status = ?2 AND o.symbolId = ?3")
    OrderEntity findFirstByBotIdAndStatusAndSymbolId(Long botId, String status, Integer symbolId);







    //
    //
    // => chỉ lấy orderEntity đầu tiên
    @Query("SELECT o FROM Order o WHERE o.account.id = ?1 AND o.status = ?2")
    OrderEntity findFirstByAccountIdAndStatus(Long accountId, String status);

    List<OrderEntity> findByBotIdAndStatusAndSymbolId(Long botId, String status, Integer symbolId);




}
