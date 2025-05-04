package luyen.tradebot.Trade.util.enumTraderBot;

import lombok.Getter;

@Getter
public enum ProtoOAExecutionType {
    ORDER_ACCEPTED(2, "Order passed validation.", "PROCESS"), // đơn hàng được chấp nhật
    ORDER_FILLED(3, "Order filled.", "OPEN"), // đơn hàng đã hoàn thành
    ORDER_CANCELLED(5, "Order cancelled.", "CANCELLED"),
    ORDER_EXPIRED(6, "Order expired.", "EXPIRED"),
    ORDER_REJECTED(7, "Order rejected.", "REJECTED"), // đơn hàng bị từ chối
    ORDER_CLOSE(-2, "Order Closed.", "CLOSE"), // đơn hàng đã được đóng
    UNKNOWN(-1, "Unknown execution type.", "NA");

    private final int code;
    private final String description;
    private final String status;

    ProtoOAExecutionType(int code, String description, String status) {
        this.code = code;
        this.description = description;
        this.status = status;
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