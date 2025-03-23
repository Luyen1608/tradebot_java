package luyen.tradebot.Trade.dto.respone;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCtraderDTO {

    private int payloadReponse;
    private String errorCode;
    private String description;
    private int payloadType;
}
