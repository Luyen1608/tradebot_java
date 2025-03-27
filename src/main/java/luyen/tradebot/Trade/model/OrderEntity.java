package luyen.tradebot.Trade.model;


import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.OrderType;
import luyen.tradebot.Trade.util.enumTraderBot.Symbol;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name ="Order")
@Getter
@Setter
@Table(name = "tbl_orders")
public class OrderEntity extends AbstractEntity {


    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Symbol symbol;

    @Column(name = "symbol_id")
    private int symbolId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TradeSide tradeSide; // BUY, SELL

    private Long botId;
    private BigDecimal volume;
    private String status; // PENDING, OPEN, CLOSED, CANCELED, ERROR
    private LocalDateTime openTime;
    private LocalDateTime closeTime;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrderType orderType; // MARKET, LIMIT, STOP

    private Double stopLoss;
    private Double takeProfit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountEntity account;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (symbol != null) {
            this.symbolId = symbol.getId();
        }
    }

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderPosition> positions = new ArrayList<>();
    private String comment;
}