package luyen.tradebot.Trade.util.enumTraderBot;

import lombok.Getter;

@Getter
public enum ProtoOAOrderStatus {
    ORDER_STATUS_ACCEPTED(1, "Order request validated and accepted for execution.."), // đơn hàng được chấp nhật
    ORDER_STATUS_FILLED(2, "Order is fully filled."), // đơn hàng bị từ chối
    ORDER_STATUS_REJECTED(3, "Order is rejected due to validation."),
    ORDER_STATUS_EXPIRED(4, "Order expired. Might be valid for orders with partially filled volume that were expired on LP."),
    ORDER_STATUS_CANCELLED(5, "Order is cancelled. Might be valid for orders with partially filled volume that were cancelled by LP."), // đơn hàng đã hoàn thành
    UNKNOWN(-1, "Unknown execution type.");

    private final int code;
    private final String description;

    ProtoOAOrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ProtoOAOrderStatus fromCode(int code) {
        for (ProtoOAOrderStatus type : ProtoOAOrderStatus.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}