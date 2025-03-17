package luyen.tradebot.Trade.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_account")
@Entity(name = "Account")
public class AccountEntity extends AbstractEntity {

    @Column(name = "account_name", length = 255)
    private String accountName;

    @Column(name = "client_id", length = 255)
    private String clientId;

    @Column(name = "secret_id", length = 255)
    private String secretId;

    @Column(name = "access_token", length = 255)
    private String accessToken;

    @Column(name = "status", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AccountStatus status;

    @Column(name = "ctid_trader_account_id", length = 255)
    private int ctidTraderAccountId;

    @Column(name = "type_account", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AccountType typeAccount;

    @Column(name = "exprixe_date")
    @Temporal(TemporalType.DATE)
    private Date exprixeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id")
    private BotEntity bot;


}
