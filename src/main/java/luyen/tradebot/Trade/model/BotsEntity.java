package luyen.tradebot.Trade.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.BotFrom;
import luyen.tradebot.Trade.util.enumTraderBot.BotStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bots")
@Entity(name = "Bots")
public class BotsEntity extends AbstractEntity {

    private String name;
    private String description;
    private String status;
    private String type;
    private String risk;
    private String signalToken;
    private String webhookUrl;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "bot")
    private List<AccountEntity> accounts = new ArrayList<>();

    public void saveAccount(AccountEntity account) {
        if (account != null) {
            accounts = new ArrayList<>();
        }
        accounts.add(account);
        account.setBot(this);

    }
}
