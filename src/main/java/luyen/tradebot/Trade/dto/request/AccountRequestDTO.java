package luyen.tradebot.Trade.dto.request;

import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;


@Getter
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class AccountRequestDTO implements Serializable {
        private String clientId;
        private String secretId;
        private String accessToken;
        private String name;
        private AccountStatus status;
        private int ctidTraderAccountId;
        private AccountType typeAccount;
        private Date expirationDate;

        private Long id;
        private String accountId;
        private boolean isActive;
        private boolean isConnected;
        private boolean isAuthenticated;
        private String connectionStatus;
        private Long botId;



}
