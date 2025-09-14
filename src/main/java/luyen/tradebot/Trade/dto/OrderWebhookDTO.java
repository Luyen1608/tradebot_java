package luyen.tradebot.Trade.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class OrderWebhookDTO {
    private String signalToken;
    private int symbol;
    private int orderType;
    private int tradeSide;
    private int stopLoss;
    private int takeProfit;
    private int relative_stop_loss;
    private int relative_take_profit;
    private int volume;
    private String id;

    private String type; // Used for "close" operations
}
