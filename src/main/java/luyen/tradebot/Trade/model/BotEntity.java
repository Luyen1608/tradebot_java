package luyen.tradebot.Trade.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.BotFrom;
import luyen.tradebot.Trade.util.enumTraderBot.BotStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_bot")
@Entity(name = "Bot")
public class BotEntity  extends AbstractEntity {

    @Column(name = "bot_name", length = 255)
    private String botName;

    @Column(name = "signal_token", length = 255)
    private String signalToken;


    @Column(name = "status", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BotStatus status;

    @Column(name = "number_account", length = 255)
    private int numberAccount;

    @Column(name = "max_account", length = 255)
    private int maxAccount;

    @Column(name = "exchange", length = 255)
    private String exchange;

    @Column(name = "bot_from", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BotFrom botFrom;

    @Column(name = "description", length = 255)
    private String description;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "bot")
    private Set<AccountEntity> accounts;

    public void saveAccount(AccountEntity account) {
        if (account != null) {
            accounts = new HashSet<>();
        }
        accounts.add(account);
        account.setBot(this);
    }
}
