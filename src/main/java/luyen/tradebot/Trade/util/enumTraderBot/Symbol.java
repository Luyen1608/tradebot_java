package luyen.tradebot.Trade.util.enumTraderBot;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Symbol {

    //Major currency pairs
    EURUSD(1),
    EURUSDT(1),
    GBPUSD(2),
    GBPUSDT(2),
    AUDUSD(5),
    AUDUSDT(5),
    XAUUSD(41),
    XAUUSDT(41),
    XPTUSD(97),
    XPTUSDT(97),
    NZDUSD(12),
    NZDUSDT(12),
    //Tien dien tu
    BTCUSD(101),
    BTCUSDT(101),
    ETHUSD(102),
    ETHUSDT(102),
    ADAUSD(433),
    ADAUSDT(433),
    DOTUSD(436),
    DOTUSDT(436),
    DOGUSD(434),
    DOGUSDT(434),
    SOLUSD(435),
    SOLUSDT(435),
    DOGEUSD(434),
    DOGEUSDT(434),
    XAGUSD(42),
    XAGUSDT(42),
    XRPUSD(414),
    XRPUSDT(414),
    BNBUSD(415),
    BNBUSDT(415),
    USDCAD(8),
    USDCHF(6),
    LTCUSD(407),
    LTCUSDT(407),
    LTCUSDT_P(407),
    TONUSD(1552),
    TONUSDT(1552),
    TONUSDT_P(1552),
    BCHUSD(400),
    BCHUSDT(400),
    BCHUSDT_P(400),
    TRXUSD(429),
    TRXUSDT(429),
    TRXUSDT_P(429),
    GBPJPY(7),
    USDJPY(4),
    UNIUSDT(439),
    LINKUSDT(437),
    AAVEUSDT(462),
    CAKEUSDT(1563),
    EURCHF(10),
    UNIUSD(439),
    LINKUSD(437),
    AAVEUSD(462),
    CAKEUSD(1563),
    ICPUSDT(1555),
    ICPUSD(1555),
    APTUSD(1556),
    APTUSDT(1556),
    EURCAD(17);

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
            // Check if the text has a . sign then split and take [0] and if there is no . sign then take it normally
            if (text.contains(".")) {
                String[] parts = text.split("\\."); // Split by dot
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    return Symbol.fromString(parts[0]); // Take the part before the dot
                }
            }
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

    //get name EURCAD theo id
    public static String getNameFromId(int id) {
        for (Symbol symbol : Symbol.values()) {
            if (symbol.getId() == id) {
                return symbol.name();
            }
        }
        throw new IllegalArgumentException("Unknown symbol ID: " + id);
    }

}