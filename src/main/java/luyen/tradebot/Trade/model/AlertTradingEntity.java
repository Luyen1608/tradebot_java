package luyen.tradebot.Trade.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.AcctionTrading;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_alert_trading")
@Entity
public class AlertTradingEntity extends AbstractEntity {

    @Column(name = "action", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AcctionTrading action;

    @Column(name = "instrument", length = 255)
    private String instrument;

    @Column(name = "timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime timestamp;

    @Column(name = "signal_token", length = 255)
    private String signalToken;

    @Column(name = "max_lag", length = 255)
    private String maxLag;

    @Column(name = "investment_type", length = 255)
    private String investmentType;

    @Column(name = "amount", length = 255)
    private Double amount;

    @Column(name = "status", length = 255)
    private String status;

    @Column(name = "stop_loss")
    private Integer stopLoss;

    @Column(name = "take_profit")
    private Integer takeProfit;

    @Column(name = "relative_stop_loss")
    private Integer relativeStopLoss;

    @Column(name = "relative_take_profit")
    private Integer relativeTakeProfit;

    @Override
    public String toString() {
        return "AlertTradingEntity{" +
                "id=" + getId() +
                ", action=" + action +
                ", instrument='" + instrument + '\'' +
                ", timestamp=" + timestamp +
                ", signalToken='" + signalToken + '\'' +
                ", maxLag='" + maxLag + '\'' +
                ", investmentType='" + investmentType + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", createAt=" + getCreateAt() +
                ", updateAt=" + getUpdateAt() +
                '}';
    }


//    id smallint NOT NULL DEFAULT nextval('tbl_alert_trading_id_seq'::regclass),
//    action e_action_trading,
//    instrument character varying(255) COLLATE pg_catalog."default",
//            "timestamp" timestamp without time zone,
//    signal_token character varying(255) COLLATE pg_catalog."default",
//    max_lag character varying(255) COLLATE pg_catalog."default",
//    investment_type character varying(255) COLLATE pg_catalog."default",
//    amount integer,
//    status character varying(255) COLLATE pg_catalog."default",
//    created_at date,
//    updated_at date,
}
