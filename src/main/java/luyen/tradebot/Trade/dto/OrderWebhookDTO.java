package luyen.tradebot.Trade.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderWebhookDTO {
    private String signalToken;
    private String symbol;
    private String orderType;
    private String tradeSide;
    private BigDecimal volume;
    private String type; // Used for "close" operations
}
