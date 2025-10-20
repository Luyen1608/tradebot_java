package luyen.tradebot.Trade.util;

import luyen.tradebot.Trade.dto.MessageTradingViewDTO;
import luyen.tradebot.Trade.dto.OrderWebhookDTO;
import luyen.tradebot.Trade.util.enumTraderBot.AcctionTrading;
import luyen.tradebot.Trade.util.enumTraderBot.OrderType;
import luyen.tradebot.Trade.util.enumTraderBot.Symbol;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
     * +    * 123.123 -> 123
     * +     * 12.12 -> 12.12
     * +
     */
    public static int roundDownToNearestThousand(double value) {
        long integerPart = (long) value;
        // 2. Xác định lũy thừa của 10 để làm tròn (roundingBase)
        long roundingBase;

        if (integerPart >= 100000000L) { // >= 10^8 (Ví dụ: 123456789)
            // Trường hợp 123456789.1234 -> 123456000 (Làm tròn đến 1000)
            roundingBase = 1000;
        } else if (integerPart >= 1000000L) { // >= 10^6 (Ví dụ: 12345678, 1234567)
            // Trường hợp 12345678.1234 -> 12345000
            // và 1234567.1234 -> 1234000.
            // Cả hai đều làm tròn đến 1000.
            roundingBase = 1000;
        } else if (integerPart >= 10000L) { // >= 10^4 (Ví dụ: 12345)
            // Trường hợp 12345.1234 -> 12000. (Làm tròn đến 1000)
            roundingBase = 1000;
        } else if (integerPart >= 1000L) { // >= 10^3 (Ví dụ: 1234)
            // Trường hợp 1234.1234 -> 1200. (Làm tròn đến 100)
            roundingBase = 100;
        } else if (integerPart >= 100L) { // >= 10^2 (Ví dụ: 123)
            // Trường hợp 123.1234 -> 123. (Làm tròn đến 1)
            roundingBase = 1;
        } else { // < 100 (Ví dụ: 12)
            // Trường hợp 12.1234 -> 12. (Làm tròn đến 1)
            roundingBase = 1;
        }
        // 3. Thực hiện phép làm tròn xuống (floor)
        // Kỹ thuật: (số / cơ số) * cơ số. Vì chúng ta đang dùng số nguyên,
        // phép chia sẽ tự động làm tròn xuống (floor).
        // Phép làm tròn: floor(integerPart / roundingBase) * roundingBase
        long result = (integerPart / roundingBase) * roundingBase;
        // 4. Ép kiểu về int và trả về (Chú ý: có thể bị tràn nếu input quá lớn)

        return (int) result;
    }

    public static OrderWebhookDTO convertTradeviewToCtrader(MessageTradingViewDTO messageTradingViewDTO) {
        BigDecimal volume = new BigDecimal(messageTradingViewDTO.getAmount());
        int volumeInt = volume.multiply(BigDecimal.valueOf(1000)).intValue();
        // kiểm tra Instrument nếu có dạng BTSUSD.xxx thì chỉ lấy BTSUSD x là số lượng k nhất định

//        if (messageTradingViewDTO.getInstrument().length() > 6) {
//            messageTradingViewDTO.setInstrument(messageTradingViewDTO.getInstrument().substring(0, 6));
//        }
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
                .id(messageTradingViewDTO.getId())
                .relative_stop_loss(roundedRelativeStopLoss)
                .relative_take_profit(roundedRelativeTakeProfit)
                .volume(volumeInt)
                .type("Order")
                .build();
        return webhookDTO;
    }

    /**
     * Convert amount string to double, handling both comma and dot as decimal separator
     * Examples:
     * "0.1" -> 0.1
     * "0,01" -> 0.01
     * "1000.50" -> 1000.50
     * "1000,50" -> 1000.50
     */
    public static Double convertAmountToDouble(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return 0.0;
        }
        try {
            // Replace comma with dot for decimal separator
            String normalizedAmount = amount.trim().replace(",", ".");
            return Double.valueOf(normalizedAmount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + amount, e);
        }
    }

    public static LocalDateTime convertStringToDateTime(String dateTimeString) {
        // Chuyển đổi chuỗi thành đối tượng LocalDateTime
        return Instant.parse(dateTimeString)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
