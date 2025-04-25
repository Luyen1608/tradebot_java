package luyen.tradebot.Trade.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_accounts")
@Entity(name = "Account")
public class AccountEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /**
     * Pre-persist hook to ensure ID is set before saving
     */
    @Column(name = "created_at", length = 255)
//    @Temporal(TemporalType.DATE)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;

    @Column(name = "updated_at", length = 255)
//    @Temporal(TemporalType.DATE)
    @UpdateTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;



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


    @OneToOne(mappedBy = "account", optional = true)
    private ConnectedEntity connecting;


    private boolean isActive;


    private String errorMessage;
    private String accountId;

}
