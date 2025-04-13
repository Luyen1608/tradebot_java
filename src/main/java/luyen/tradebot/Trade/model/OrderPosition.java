package luyen.tradebot.Trade.model;


import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity(name = "OrderPosition")
@Table(name = "tbl_order_positions")
public class OrderPosition extends AbstractEntity{


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountEntity account;

    private Integer positionId;

    //setting default value
    private String errorCode;

    private String executionType;

    private String payloadType;

    @Column(name = "order_ctrader_id")
    private int orderCtraderId;

    @Column(name = "client_msg_id")
    private String clientMsgId;


    private String status; // PENDING, OPEN, CLOSED, ERROR
    private String errorMessage;




}