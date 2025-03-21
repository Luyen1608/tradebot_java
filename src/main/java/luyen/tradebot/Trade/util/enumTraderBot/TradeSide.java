package luyen.tradebot.Trade.util.enumTraderBot;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TradeSide {
    BUY(1),
    SELL(2);

    private final int value;

    TradeSide(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static TradeSide fromValue(int value) {
        for (TradeSide side : TradeSide.values()) {
            if (side.getValue() == value) {
                return side;
            }
        }
        throw new IllegalArgumentException("Unknown trade side value: " + value);
    }

    public static TradeSide fromString(String text) {
        for (TradeSide side : TradeSide.values()) {
            if (side.name().equalsIgnoreCase(text)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Unknown trade side: " + text);
    }
}