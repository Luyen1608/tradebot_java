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
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    List<OrderEntity> findByAccountId(UUID accountId);

    List<OrderEntity> findByStatus(String status);

    @Query("SELECT o FROM Order o JOIN o.account a WHERE a.bot.id = ?1 AND o.symbolId = ?2 AND o.status = 'OPEN'")
    List<OrderEntity> findOpenOrdersByBotIdAndSymbolId(UUID botId, Integer symbolId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.positions p WHERE o.symbolId = ?1 AND o.tradeSide = ?2 " +
            "AND o.status = 'OPEN' AND o.account.bot.signalToken = ?3 ORDER BY o.createAt desc limit 1")
    Optional<OrderEntity>  findOpenOrdersBySymbolIdAndBotSignalTokenAndTradeSide(Integer symbolId, TradeSide tradeSide, String signalToken);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :status " +
            " WHERE o.id = :id")
    int updateStatusById(@Param("status") String status,
                         @Param("id") UUID id);

    //lấy row đàu tiên by bot id and status and symbolId
    @Query("SELECT o FROM Order o WHERE o.botId = ?1 AND o.status = ?2 AND o.symbolId = ?3")
    OrderEntity findFirstByBotIdAndStatusAndSymbolId(UUID botId, String status, Integer symbolId);


    @Query("SELECT o FROM Order o WHERE o.account.id = ?1 AND o.status = ?2")
    OrderEntity findFirstByAccountIdAndStatus(UUID accountId, String status);

    List<OrderEntity> findByBotIdAndStatusAndSymbolId(UUID botId, String status, Integer symbolId);


}
