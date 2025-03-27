package luyen.tradebot.Trade.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import luyen.tradebot.Trade.util.enumTraderBot.OrderType;
import luyen.tradebot.Trade.util.enumTraderBot.Symbol;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;

import java.math.BigDecimal;

@Data
@Builder
@Getter
@Setter
public class OrderWebhookDTO {
    private String signalToken;
    private int symbol;
    private int orderType;
    private int tradeSide;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private int volume;

    private String type; // Used for "close" operations
}
