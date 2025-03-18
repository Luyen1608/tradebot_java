package luyen.tradebot.Trade.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;

import java.io.Serializable;
import java.util.Date;


@Getter
@Builder
@AllArgsConstructor
public class AccountRequestDTO implements Serializable {
        private String clientId;
        private String secretId;
        private String accessToken;
        private String name;
        private AccountStatus status;
        private int ctidTraderAccountId;
        private AccountType typeAccount;
        private Date expirationDate;


}
