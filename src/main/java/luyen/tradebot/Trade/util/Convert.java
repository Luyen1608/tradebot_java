package luyen.tradebot.Trade.util;

import luyen.tradebot.Trade.dto.MessageTradingViewDTO;
import luyen.tradebot.Trade.dto.OrderWebhookDTO;
import luyen.tradebot.Trade.util.enumTraderBot.AcctionTrading;
import luyen.tradebot.Trade.util.enumTraderBot.OrderType;
import luyen.tradebot.Trade.util.enumTraderBot.Symbol;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Convert {
    /**
     * +     * Rounds down a number to the nearest thousand
     * +     * Examples:
     * +     * 3478958 -> 3478000
     * +     * 34789589 -> 34789000
     * +     * 347895899 -> 347895000
     * +
     */
    public static int roundDownToNearestThousand(double value) {
        return (int) (Math.floor(value / 1000) * 1000);
    }

    public static OrderWebhookDTO convertTradeviewToCtrader(MessageTradingViewDTO messageTradingViewDTO) {
        BigDecimal volume = new BigDecimal(messageTradingViewDTO.getAmount());
        int volumeInt = volume.multiply(BigDecimal.valueOf(1000)).intValue();
        // kiểm tra Instrument nếu có dạng BTSUSD.xxx thì chỉ lấy BTSUSD x là số lượng k nhất định

        if (messageTradingViewDTO.getInstrument().length() > 6) {
            messageTradingViewDTO.setInstrument(messageTradingViewDTO.getInstrument().substring(0, 6));
        }
        // Kiểm tra và parse các giá trị, sử dụng giá trị mặc định nếu null
        double relativeTakeProfit = 0;
        double relativeStopLoss = 0;
        double takeProfit = 0;
        double stopLos = 0;
        
        if (messageTradingViewDTO.getRelative_take_profit() != null && !messageTradingViewDTO.getRelative_take_profit().trim().isEmpty()) {
            relativeTakeProfit = Double.parseDouble(messageTradingViewDTO.getRelative_take_profit());
        }
        
        if (messageTradingViewDTO.getRelative_stop_loss() != null && !messageTradingViewDTO.getRelative_stop_loss().trim().isEmpty()) {
            relativeStopLoss = Double.parseDouble(messageTradingViewDTO.getRelative_stop_loss());
        }
        
        if (messageTradingViewDTO.getTake_profit() != null && !messageTradingViewDTO.getTake_profit().trim().isEmpty()) {
            takeProfit = Double.parseDouble(messageTradingViewDTO.getTake_profit());
        }
        
        if (messageTradingViewDTO.getStop_loss() != null && !messageTradingViewDTO.getStop_loss().trim().isEmpty()) {
            stopLos = Double.parseDouble(messageTradingViewDTO.getStop_loss());
        }
        
        int roundedRelativeTakeProfit = roundDownToNearestThousand(relativeTakeProfit);
        int roundedRelativeStopLoss = roundDownToNearestThousand(relativeStopLoss);
        OrderWebhookDTO webhookDTO = OrderWebhookDTO.builder()
                .symbol(Symbol.fromString(messageTradingViewDTO.getInstrument()).getId())
                .tradeSide(TradeSide.fromString(AcctionTrading.fromString(messageTradingViewDTO.getAction()).getValue()).getValue())
                .signalToken(messageTradingViewDTO.getSignalToken())
                .orderType(OrderType.fromString(messageTradingViewDTO.getOrderType()).getValue())
                .stopLoss((int) stopLos)
                .takeProfit((int) takeProfit)
                .relative_stop_loss(roundedRelativeStopLoss)
                .relative_take_profit(roundedRelativeTakeProfit)
                .volume(volumeInt)
                .type("Order")
                .build();
        return webhookDTO;
    }

    public static LocalDateTime convertStringToDateTime(String dateTimeString) {
        // Chuyển đổi chuỗi thành đối tượng LocalDateTime
        return Instant.parse(dateTimeString)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
