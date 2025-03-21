package luyen.tradebot.Trade.util.enumTraderBot;


import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderType {
    MARKET(1),
    LIMIT(2),
    STOP(3),
    STOP_LIMIT(4);

    private final int value;

    OrderType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static OrderType fromValue(int value) {
        for (OrderType type : OrderType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown order type value: " + value);
    }

    public static OrderType fromString(String text) {
        for (OrderType type : OrderType.values()) {
            if (type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown order type: " + text);
    }
}