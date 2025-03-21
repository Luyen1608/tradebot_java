package luyen.tradebot.Trade.model;


import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "OrderPosition")
@Table(name = "tbl_order_positions")
public class OrderPosition extends AbstractEntity{


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountEntity account;

    private String positionId;
    private String status; // PENDING, OPEN, CLOSED, ERROR
    private String errorMessage;
}