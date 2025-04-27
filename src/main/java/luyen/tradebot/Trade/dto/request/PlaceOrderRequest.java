package luyen.tradebot.Trade.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.OrderEntity;
import luyen.tradebot.Trade.service.CTraderConnection;

@Getter
@Setter
@Builder
public class PlaceOrderRequest {
    private CTraderConnection connection;
    private String clientMsgId;
    private int symbol;
    private int tradeSide;
    private int volume;
    private int orderType;
    private AccountEntity account;
    private OrderEntity savedOrder;


}
