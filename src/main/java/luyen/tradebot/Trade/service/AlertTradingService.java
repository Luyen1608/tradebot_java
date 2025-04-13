package luyen.tradebot.Trade.service;

import luyen.tradebot.Trade.model.AlertTradingEntity;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface AlertTradingService {
    Page<AlertTradingEntity> getAlertTradings(int pageNo, int pageSize, String search, String sorts, LocalDateTime createdAt);
}