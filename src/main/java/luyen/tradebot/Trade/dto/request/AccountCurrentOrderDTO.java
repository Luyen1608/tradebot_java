package luyen.tradebot.Trade.dto.request;

import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;


@Getter
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class AccountCurrentOrderDTO implements Serializable {
        private int ctidTraderAccountId;
        private long fromTimestamp;
        private long toTimestamp;




}
