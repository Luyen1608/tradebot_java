package luyen.tradebot.Trade.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderWebhookDTO {
    private String signalToken;
    private Integer symbolId;
    private Integer orderType;
    private Integer tradeSide;
    private BigDecimal volume;
    private String type; // Used for "close" operations
}
