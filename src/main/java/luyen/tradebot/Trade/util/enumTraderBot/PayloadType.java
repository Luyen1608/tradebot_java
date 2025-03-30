package luyen.tradebot.Trade.util.enumTraderBot;

public enum PayloadType {

    PROTO_OA_EXECUTION_EVENT(2126),
    PROTO_OA_ACCOUNT_AUTH_REQ(2102),
    PROTO_OA_ACCOUNT_AUTH_RES(2103),
    PROTO_OA_ORDER_ERROR_EVENT(2132),
    UNKNOWN(-1);

    private final int value;

    PayloadType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    //get name
    public String getName() {
        return name();
    }
    public static PayloadType fromValue(int value) {
        for (PayloadType type : PayloadType.values()) {
            if (type.value == value) {
                return type;
            }
        }
       return null;
    }
    public static PayloadType fromName(String name) {
        for (PayloadType type : PayloadType.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

}
