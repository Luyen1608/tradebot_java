package luyen.tradebot.Trade.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "error_log")
@Entity(name = "Error")
public class ErrorLogEntity extends AbstractEntity {
    private String accountId;
    private String botId;
    private String errorCode;
    private String errorMessage;
}
