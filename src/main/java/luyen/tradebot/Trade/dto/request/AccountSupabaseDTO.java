package luyen.tradebot.Trade.dto.request;

import jakarta.validation.constraints.Null;
import lombok.*;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;


@Getter
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class AccountSupabaseDTO implements Serializable {
    private UUID id;
    private UUID botId;
    private String accountidTrading;
    private UUID userId;
    private LocalDateTime addedDate;
    private String status;

    // có thể không có active
    @Null
    private Boolean active;

    private Double volumeMultiplier;
    private UUID apiConnectId;
    private String signalToken;
    private String clientId;
    private String secretId;
    private String accessToken;
    private Boolean live;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
