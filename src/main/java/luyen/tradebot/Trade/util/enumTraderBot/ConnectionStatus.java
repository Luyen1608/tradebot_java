package luyen.tradebot.Trade.util.enumTraderBot;

/**
 * Enum định nghĩa các trạng thái kết nối của tài khoản cTrader
 */
public enum ConnectionStatus {
    CONNECTED,      // Tài khoản đã kết nối thành công
    DISCONNECTED,   // Tài khoản đã ngắt kết nối
    PENDING,        // Đang trong quá trình kết nối, chờ xác nhận
    RECONNECTING,   // Đang trong quá trình kết nối lại sau khi mất kết nối
    ERROR           // Có lỗi xảy ra khi kết nối
}
