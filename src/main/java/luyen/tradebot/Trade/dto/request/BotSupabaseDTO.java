package luyen.tradebot.Trade.dto.request;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class BotSupabaseDTO implements Serializable {
    private UUID id;
    private UUID botId;

    private String name;
    private String description;
    private String status;
    private String type;
    private String risk;
    private String signalToken;
    private String webhookUrl;
    private Boolean isDeleted;
    private UUID ownerId;
    private String signalFrom;
    private String colorScheme;
    private Boolean isBestSeller;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;





}

