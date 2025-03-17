package luyen.tradebot.Trade.model;

import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.AcctionTrading;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_alert_trading")
@Entity(name = "trading")
public class AlertTradingEntity extends AbstractEntity {

    @Column(name = "action", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AcctionTrading action;

    @Column(name = "instrument", length = 255)
    private String instrument;

    @Column(name = "timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDate timestamp;

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
