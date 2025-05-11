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
    public static OrderWebhookDTO convertTradeviewToCtrader(MessageTradingViewDTO messageTradingViewDTO) {
        BigDecimal volume = new BigDecimal(messageTradingViewDTO.getAmount());
        int volumeInt = volume.multiply(BigDecimal.valueOf(1000)).intValue();
        // kiểm tra Instrument nếu có dạng BTSUSD.xxx thì chỉ lấy BTSUSD x là số lượng k nhất định

        if (messageTradingViewDTO.getInstrument().length() > 6) {
            messageTradingViewDTO.setInstrument(messageTradingViewDTO.getInstrument().substring(0, 6));
        }
        OrderWebhookDTO webhookDTO = OrderWebhookDTO.builder()
                .symbol(Symbol.fromString(messageTradingViewDTO.getInstrument()).getId())
                .tradeSide(TradeSide.fromString(AcctionTrading.fromString(messageTradingViewDTO.getAction()).getValue()).getValue())
                .signalToken(messageTradingViewDTO.getSignalToken())
                .orderType(OrderType.fromString(messageTradingViewDTO.getOrderType()).getValue())
                .stopLoss((int)Double.parseDouble(messageTradingViewDTO.getRelative_stop_loss()))
                .takeProfit((int)Double.parseDouble(messageTradingViewDTO.getTake_profit()))
                .relative_stop_loss((int)Double.parseDouble(messageTradingViewDTO.getRelative_stop_loss()))
                .relative_take_profit((int)Double.parseDouble(messageTradingViewDTO.getRelative_take_profit()))
                .volume(volumeInt)
                .type("Order")
                .build();
        return webhookDTO;
    }
    public static LocalDateTime  convertStringToDateTime(String dateTimeString) {
        // Chuyển đổi chuỗi thành đối tượng LocalDateTime
        return Instant.parse(dateTimeString)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
