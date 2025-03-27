package luyen.tradebot.Trade.util.enumTraderBot;

public enum ErrorCode {

    NOT_ENOUGH_MONEY("Not enough funds to allocate margin.");

    ErrorCode(String s) {
    }
    //tạo thêm các function get giá trị và text theo enum
    public String getValue() {
        return this.name();
    }
    public String getText() {
        return this.toString();
    }
    public static ErrorCode fromValue(String value) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getValue().equals(value)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Invalid ErrorCode value: " + value);
    }
    //fromString
    public static ErrorCode fromString(String text) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getText().equals(text)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Invalid ErrorCode text: " + text);
    }
}
