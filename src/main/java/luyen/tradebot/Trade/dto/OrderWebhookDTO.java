package luyen.tradebot.Trade.dto;

import lombok.Data;
import luyen.tradebot.Trade.util.enumTraderBot.OrderType;
import luyen.tradebot.Trade.util.enumTraderBot.Symbol;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;

import java.math.BigDecimal;

@Data
public class OrderWebhookDTO {
    private String signalToken;
    private String symbol;
    private String orderType;
    private String tradeSide;
    private BigDecimal volume;
    private String type; // Used for "close" operations
}
