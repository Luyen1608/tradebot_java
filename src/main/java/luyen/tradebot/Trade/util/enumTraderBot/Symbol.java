package luyen.tradebot.Trade.util.enumTraderBot;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Symbol {
    BTCUSD(101),
    XAUUSD(41),
    ADAUSD(433);
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





}