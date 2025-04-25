package luyen.tradebot.Trade.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.BotFrom;
import luyen.tradebot.Trade.util.enumTraderBot.BotStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bots")
@Entity(name = "Bots")
public class BotsEntity  {

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


    private String name;
    private String description;
    private String status;
    private String type;
    private String risk;
    private String signalToken;
    private String webhookUrl;
    private UUID botId;
    private Boolean isDeleted;

    private UUID ownerId;
    private Boolean isBestSeller;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER, mappedBy = "bot")
    private List<AccountEntity> accounts = new ArrayList<>();

    public void saveAccount(AccountEntity account) {
        if (account != null) {
            accounts = new ArrayList<>();
        }
        accounts.add(account);
        account.setBot(this);

    }
}
