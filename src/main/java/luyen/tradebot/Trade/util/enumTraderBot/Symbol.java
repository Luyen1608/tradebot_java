package luyen.tradebot.Trade.util.enumTraderBot;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Symbol {

    //Major currency pairs
    EURUSD(1),
    GBPUSD(2),
    USDJPY(4),
    USDCHF(6),
    AUDUSD(5),
    USDCAD(8),
    XAUUSD(41),
    XAGUSD(42),
    XPTUSD(97),
    NZDUSD(12),
    //Tien dien tu
    BTCUSD(101),
    ETHUSD(102),
    XRPUSD(414),
    LTCUSD(407),
    ADAUSD(433),
    DOTUSD(436),
    SOLUSD(435),
    DOGUSD(434),
    BNBUSD(415),
    TRXUSD(429),
    TONUSD(1552);
    private final int id;

    Symbol(int id) {
        this.id = id;
    }

    @JsonValue
    public int getId() {
        return id;
    }

    public static Symbol fromId(int id) {
        for (Symbol symbol : Symbol.values()) {
            if (symbol.getId() == id) {
                return symbol;
            }
        }
        throw new IllegalArgumentException("Unknown symbol ID: " + id);
    }

    public static Symbol fromString(String text) {
        try {
            return Symbol.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown symbol: " + text);
        }
    }

    //fromString full ví dụ BTCUSDT.P se ra BTCUSD
    public static Symbol fromStringFull(String text) {
        try {
            String[] parts = text.split("\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid symbol format: " + text);
            }
            return Symbol.fromString(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown symbol: " + text);
        }
    }

    //fromstring luon lấy 6 ký tự dau để so sánh BTCUSDDT
    public static Symbol fromString6(String text) {
        try {
            return Symbol.valueOf(text.substring(0, 6).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown symbol: " + text);
        }
    }


}