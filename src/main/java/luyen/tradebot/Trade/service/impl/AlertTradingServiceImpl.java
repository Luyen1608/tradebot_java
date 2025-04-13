package luyen.tradebot.Trade.service.impl;

import luyen.tradebot.Trade.model.AlertTradingEntity;
import luyen.tradebot.Trade.repository.AlertTradingRepository;
import luyen.tradebot.Trade.service.AlertTradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AlertTradingServiceImpl implements AlertTradingService {

    private final AlertTradingRepository alertTradingRepository;

    @Autowired
    public AlertTradingServiceImpl(AlertTradingRepository alertTradingRepository) {
        this.alertTradingRepository = alertTradingRepository;
    }

    @Override
    public Page<AlertTradingEntity> getAlertTradings(int pageNo, int pageSize, String search, String sorts, LocalDateTime timestamp) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return alertTradingRepository.findAll(pageable, search, sorts, timestamp);
    }
}
