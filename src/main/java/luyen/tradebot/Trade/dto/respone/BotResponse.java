package luyen.tradebot.Trade.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import luyen.tradebot.Trade.util.enumTraderBot.BotFrom;
import luyen.tradebot.Trade.util.enumTraderBot.BotStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
public class BotResponse implements Serializable {
    private long id;
    private String botName;
    private String signalToken;
    private BotStatus status;
    private int numberAccount;
    private int maxAccount;
    private LocalDateTime createDate;
    private String exchange;
    private BotFrom botFrom;
    private String description;
    private String webhook;
}
