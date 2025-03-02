package luyen.tradebot.Trade.dto.respone;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ResponseData<T> {
    private int status;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    // delete put patch
    public ResponseData(int status, String message) {
        this.status = status;
        this.message = message;
    }
    // get, post
    public ResponseData(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
