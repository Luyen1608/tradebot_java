package luyen.tradebot.Trade.dto.request;

import lombok.Data;
import luyen.tradebot.Trade.util.enumTraderBot.OrderType;
import luyen.tradebot.Trade.util.enumTraderBot.Symbol;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;

import java.math.BigDecimal;

@Data
public class OrderDTO {
    private Symbol symbol;
    private TradeSide tradeSide;
    private OrderType orderType;
    private BigDecimal volume;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private String botName;

}
