package luyen.tradebot.Trade.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.BotFrom;
import luyen.tradebot.Trade.util.enumTraderBot.BotStatus;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BotRequestDTO implements Serializable {

    @NotBlank(message = "Bot Name not Blank")
    private String botName;

    @NotBlank(message = "signal Token not Blank")
    private String signalToken;

//    @NotBlank(message = "Status not Blank")
    private BotStatus botStatus;

    @NotNull(message = "Max Account not Blank")
    private int maxAccount;

    @NotBlank(message = "Exchange not Blank")
    private String exchange;

//    @NotBlank(message = "Bot from not Blank")
    private BotFrom botFrom;

    @NotBlank(message = "Description not Blank")
    private String description;

    @NotEmpty
    private Set<AccountRequestDTO> accountRequests;
}
