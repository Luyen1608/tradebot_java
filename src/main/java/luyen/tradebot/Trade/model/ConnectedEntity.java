package luyen.tradebot.Trade.model;

import jakarta.persistence.*;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.ConnectStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.UUID;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_connected")
@Entity(name = "connected")
public class ConnectedEntity extends AbstractEntity{


    public ConnectedEntity(UUID accountId, ConnectStatus connectionStatus) {
        this.account.setId(accountId);
        this.connectionStatus = connectionStatus;
    }

    @Column(name = "bot_name", length = 255)
    private String botName;

    @Column(name = "account_name", length = 255)
    private String accountName;

    @OneToOne
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private AccountEntity account;

    @Column(name = "connection_status", length = 255)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ConnectStatus connectionStatus;

    @Column(name = "last_connection_time", length = 255)
    private Date lastConnectionTime;

    @Column(name = "last_disconnection_time", length = 255)
    private Date lastDisconnectionTime;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Column(name = "error_code", length = 255)
    private String errorCode;

}
