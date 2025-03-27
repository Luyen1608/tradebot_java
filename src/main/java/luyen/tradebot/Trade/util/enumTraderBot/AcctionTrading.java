package luyen.tradebot.Trade.util.enumTraderBot;

public enum AcctionTrading {
    ENTER_LONG("BUY"),
    ENTER_SHORT("SELL");

    AcctionTrading(String value) {
    }
    //get value
    public String getValue() {
        return this.name();
    }
    public static AcctionTrading fromValue(String value) {
        for (AcctionTrading action : AcctionTrading.values()) {
            if (action.getValue().equalsIgnoreCase(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown action: " + value);
    }
    public static AcctionTrading fromString(String text) {
        for (AcctionTrading action : AcctionTrading.values()) {
            if (action.name().equalsIgnoreCase(text)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown action: " + text);
    }

}
