package luyen.tradebot.Trade.repository;

import jakarta.transaction.Transactional;
import luyen.tradebot.Trade.model.OrderPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderPositionRepository extends JpaRepository<OrderPosition, UUID> {
    @Query(value = "SELECT op FROM OrderPosition op WHERE op.clientMsgId = ?1 ORDER BY op.createAt ASC LIMIT 1")
    Optional<OrderPosition> findByClientMsgId(String clientMsgId);

    @Query(value = "SELECT op FROM OrderPosition op WHERE op.clientMsgId = ?1 ORDER BY op.createAt ASC LIMIT 1")
    Optional<OrderPosition> findByClientMsgIdLimitOne(String clientMsgId);

    List<OrderPosition> findByOrderId(UUID orderId);

    List<OrderPosition> findByAccountId(UUID accountId);

    @Query("SELECT op FROM OrderPosition op WHERE op.order.symbolId = ?1 AND op.account.id = ?2 AND op.status = 'OPEN'")
    List<OrderPosition> findOpenPositionsBySymbolIdAndAccountId(Integer symbolId, UUID accountId);

    @Query("SELECT op FROM OrderPosition op WHERE op.order.id = ?1 AND op.account.id = ?2")
    Optional<OrderPosition> findByOrderIdAndAccountId(UUID orderId, UUID accountId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE OrderPosition op SET op.executionType = :executionType " +
            "WHERE op.orderCtraderId = :orderCtraderId AND op.positionId = :positionId")
    int updateExecutionTypeByOrderCtraderIdAndPositionId(@Param("executionType") String executionType,
                                                         @Param("orderCtraderId") Integer orderCtraderId,
                                                         @Param("positionId") Integer positionId);


    // update executionType và errorMessage theo positionId và orderCtraderId
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE OrderPosition op SET op.executionType = :executionType, " +
                        "op.errorMessage = :errorMessage, " +
                        "op.errorCode = :errorCode, " +
                        "op.status = :orderStatus " +
                        "WHERE op.clientMsgId = :clientMsgId")
    int updateByOrderCtraderIdAndPositionId(
            @Param("executionType") String executionType,
            @Param("errorMessage") String errorMessage,
            @Param("errorCode") String errorCode,
            @Param("orderStatus") String orderStatus,
            @Param("clientMsgId") String clientMsgId);


    // update errorcode and errormessage by clientMsgId
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE OrderPosition op SET op.status = :orderStatus, op.errorCode = :errorCode, op.errorMessage = :errorMessage " +
            "WHERE op.clientMsgId = :clientMsgId")
    void updateErrorCodeAndErrorMessageByClientMsgId(@Param("errorCode") String errorCode,
                                                    @Param("errorMessage") String errorMessage,
                                                    @Param("orderStatus") String orderStatus,
                                                    @Param("clientMsgId") String clientMsgId);


    //lấy OrderPosition có orderId = orders.getId và status = Open
    @Query("SELECT op FROM OrderPosition op WHERE op.order.id = ?1 AND op.status = ?2 ")
    List<OrderPosition> findByOrderIdAndStatus(UUID orderId, String status);
    //    AND op.status = ?2
    @Query("SELECT op FROM OrderPosition op JOIN FETCH op.account  WHERE op.order.id = ?1 AND UPPER(op.orderType) = ?2 ")
    List<OrderPosition> findByOrderIdAndStatusAndOrderType(UUID orderId, String orderType);


}