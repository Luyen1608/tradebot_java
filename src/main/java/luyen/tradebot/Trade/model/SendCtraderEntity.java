package luyen.tradebot.Trade.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_send_ctrader")
@Entity(name = "ctrader")
public class SendCtraderEntity extends AbstractEntity {

    @Column(name = "original_signal_id", length = 255)
    private int originalSignalId;

    @Column(name = "trade_side", length = 255)
    private String tradeSide;

    @Column(name = "order_type", length = 255)
    private String orderType;

    @Column(name = "symbol_id", length = 255)
    private int symbolId;

    @Column(name = "signal_token", length = 255)
    private String signalToken;

    @Column(name = "volum", length = 255)
    private Double volum;

    @Column(name = "ctid_trader_account_id", length = 255)
    private String ctidTraderAccountId;

    @Column(name = "status", length = 255)
    private String status;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id")
    private BotEntity bot_id;

}
