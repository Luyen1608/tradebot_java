package luyen.tradebot.Trade.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

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
    private int ctidTraderAccountId;

    @Column(name = "type_account", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AccountType typeAccount;

    @Column(name = "token_expiry")
    private Date tokenExpiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id")
    private BotEntity bot;


    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL,mappedBy = "connected")
    @JoinColumn(name = "connected_id", referencedColumnName = "id")
    private ConnectedEntity connected;


    private boolean isActive;
    private boolean isConnected;
    private Date lastConnected;
    private String errorMessage;
    private String accountId;
    private boolean authenticated;

}
