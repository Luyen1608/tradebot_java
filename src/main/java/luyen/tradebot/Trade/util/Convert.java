package luyen.tradebot.Trade.util;

import luyen.tradebot.Trade.dto.MessageTradingViewDTO;
import luyen.tradebot.Trade.dto.OrderWebhookDTO;
import luyen.tradebot.Trade.util.enumTraderBot.AcctionTrading;
import luyen.tradebot.Trade.util.enumTraderBot.OrderType;
import luyen.tradebot.Trade.util.enumTraderBot.Symbol;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;

import java.math.BigDecimal;

public class Convert {
    public static OrderWebhookDTO convertTradeviewToCtrader(MessageTradingViewDTO messageTradingViewDTO) {
        BigDecimal volume = new BigDecimal(messageTradingViewDTO.getAmount());
        int volumeInt = volume.multiply(BigDecimal.valueOf(1000)).intValue();
        // kiểm tra Instrument nếu có dạng BTSUSD.xxx thì chỉ lấy BTSUSD x là số lượng k nhất định
        if (messageTradingViewDTO.getInstrument().contains(".")) {
            String[] parts = messageTradingViewDTO.getInstrument().split("\\.");
            messageTradingViewDTO.setInstrument(parts[0]);
        }
        OrderWebhookDTO webhookDTO = OrderWebhookDTO.builder()
                .symbol(Symbol.fromString(messageTradingViewDTO.getInstrument()).getId())
                .tradeSide(TradeSide.fromString(AcctionTrading.fromString(messageTradingViewDTO.getAction()).toString()).getValue())
                .signalToken(messageTradingViewDTO.getSignalToken())
                .orderType(OrderType.fromString(messageTradingViewDTO.getOrderType()).getValue())
                .volume(volumeInt)
                .type("Order")
                .build();
        return webhookDTO;
    }
}
