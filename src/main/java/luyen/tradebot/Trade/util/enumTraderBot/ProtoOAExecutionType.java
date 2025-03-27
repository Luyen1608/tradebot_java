package luyen.tradebot.Trade.util.enumTraderBot;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProtoOAExecutionType {
    ORDER_ACCEPTED(2, "Order passed validation."),
    ORDER_EXECUTED(3, "Order executed."),
    ORDER_REJECTED(7, "Order rejected."),
    ORDER_CANCELLED(5, "Order cancelled."),
    ORDER_EXPIRED(6, "Order expired."),
    ORDER_FILLED(3, "Order filled."),
    UNKNOWN(-1, "Unknown execution type.");

    private final int code;
    private final String description;

    ProtoOAExecutionType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ProtoOAExecutionType fromCode(int code) {
        for (ProtoOAExecutionType type : ProtoOAExecutionType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}