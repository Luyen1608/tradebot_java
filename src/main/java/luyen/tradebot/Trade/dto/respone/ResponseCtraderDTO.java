package luyen.tradebot.Trade.dto.respone;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseCtraderDTO {

    private int payloadReponse;
    private String clientMsgId;
    private String errorCode;
    private String description;
    private int payloadType;
    private int executionType;
    private int positionId;
    private String systemType;
}
