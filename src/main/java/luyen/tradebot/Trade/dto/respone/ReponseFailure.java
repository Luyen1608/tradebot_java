package luyen.tradebot.Trade.dto.respone;

import org.springframework.http.HttpStatusCode;

public class ReponseFailure extends ResponseSuccess{

    public ReponseFailure(HttpStatusCode status, String message) {
        super(status, message);
    }
}
