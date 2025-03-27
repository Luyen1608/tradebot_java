package luyen.tradebot.Trade.dto.respone;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class OrderResponseCtrader {
    @JsonProperty("payloadType")
    private int payloadType;

    @JsonProperty("clientMsgId")
    private String clientMsgId;


    @JsonProperty("payload")
    private Payload payload;

    public int getPayloadType() { return payloadType; }
    public String getClientMsgId() { return clientMsgId; }
    public Payload getPayload() { return payload; }

    public static class Payload {
        @JsonProperty("ctidTraderAccountId")
        private long ctidTraderAccountId;

        @JsonProperty("executionType")
        private String executionType;

        @JsonProperty("position")
        private Position position;

        @JsonProperty("errorCode")
        private String errorCode;

        public long getCtidTraderAccountId() { return ctidTraderAccountId; }
        public Position getPosition() { return position; }
        public String getErrorCode() { return errorCode; }
    }

    public static class Position {
        @JsonProperty("positionId")
        private Integer positionId;

        public Integer getPositionId() { return positionId; }
    }
}