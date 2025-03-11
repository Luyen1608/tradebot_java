package luyen.tradebot.Trade.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AccountChooseResponse {

    private String ctidTraderAccountId;
    private String type; //demo live isLive true
    private String traderLogin;

}
