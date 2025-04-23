package luyen.tradebot.Trade.model;

import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_accounts")
@Entity(name = "Account")
public class AccountEntity extends AbstractEntity {

    @Column(name = "account_name", length = 255)
    private String accountName;

    @Column(name = "client_id", length = 255)
    private String clientId;

    @Column(name = "client_secret", length = 255)
    private String clientSecret;

    @Column(name = "access_token", length = 255)
    private String accessToken;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    @Column(name = "connection_status", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AccountStatus connectionStatus;

    @Column(name = "ctid_trader_account_id", length = 255)
    private Integer ctidTraderAccountId;

    private Integer traderLogin;

    private Double volumeMultiplier;


    @Column(name = "type_account", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AccountType typeAccount;

    @Column(name = "token_expiry")
    private Date tokenExpiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id")
    private BotsEntity bot;


    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    private ConnectedEntity connecting;


    private boolean isActive;
    private boolean isConnected;
    private Date lastConnected;
    private String errorMessage;
    private String accountId;
    private boolean authenticated;

}
