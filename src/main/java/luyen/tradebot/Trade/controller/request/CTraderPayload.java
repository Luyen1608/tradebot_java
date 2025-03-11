package luyen.tradebot.Trade.controller.request;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CTraderPayload {
    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("clientSecret")
    private String clientSecret;

    @JsonProperty("accessToken")
    private String accessToken;

    public CTraderPayload(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public CTraderPayload(String clientId, String clientSecret, String accessToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = accessToken;
    }
}



