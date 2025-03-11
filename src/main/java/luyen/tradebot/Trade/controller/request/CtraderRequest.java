package luyen.tradebot.Trade.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CtraderRequest {
    @JsonProperty("clientMsgId")
    private String clientMsgId;

    @JsonProperty("payloadType")
    private int payloadType;

    @JsonProperty("payload")
    private CTraderPayload payload;

    public CtraderRequest(String clientMsgId, int payloadType, CTraderPayload payload) {
        this.clientMsgId = clientMsgId;
        this.payloadType = payloadType;
        this.payload = payload;
    }

    public String convertToJson(Object request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(this);
        return json;
    }
}