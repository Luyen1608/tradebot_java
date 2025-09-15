package luyen.tradebot.Trade.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageTradingViewDTO {

    //    {
//        "action": "ENTER_LONG",
//            "instrument": "ADAUSDT.P",
//            "timestamp": "2025-03-23T04:42:00Z",
//            "signalToken": "ultra2in1-altcoin",
//            "maxLag": "300",
//            "investmentType": "contract",
//            "amount": "10000"
//    }
    private String action;
    private String instrument;
    private String timestamp;
    private String signalToken;
    private String maxLag;
    private String investmentType;
    private String amount;
    private String orderType = "MARKET";
    private String relative_stop_loss;
    private String relative_take_profit;
    private String stop_loss;
    private String take_profit;

    private String id ="-1";
}
