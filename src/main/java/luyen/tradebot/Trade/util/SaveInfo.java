package luyen.tradebot.Trade.util;

import lombok.AllArgsConstructor;
import luyen.tradebot.Trade.model.AlertTradingEntity;
import luyen.tradebot.Trade.repository.AlertTradingRepository;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class SaveInfo {

    private final AlertTradingRepository alertTradingRepository;

    public AlertTradingEntity save(AlertTradingEntity  alertTradingEntity) {
        return alertTradingRepository.save(alertTradingEntity);
    }
}
