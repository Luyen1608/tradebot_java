package luyen.tradebot.Trade.util.enumTraderBot;

public enum ErrorCode {

    NOT_ENOUGH_MONEY("Not enough funds to allocate margin."),
    TRADING_BAD_VOLUME("Invalid volume"),
    POSITION_NOT_FOUND("Position not found.");
    private final String description;
    ErrorCode(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
    public String getName() {
        return name();
    }
    public static ErrorCode fromName(String name) {
        for (ErrorCode type : ErrorCode.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ErrorCode: " + name);
    }
    public static ErrorCode fromCode(int code) {
        for (ErrorCode type : ErrorCode.values()) {
            if (type.ordinal() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ErrorCode: " + code);
    }
}
