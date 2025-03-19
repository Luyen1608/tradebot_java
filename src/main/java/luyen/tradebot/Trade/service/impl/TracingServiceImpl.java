package luyen.tradebot.Trade.service.impl;

import lombok.AllArgsConstructor;
import luyen.tradebot.Trade.model.AlertTradingEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import luyen.tradebot.Trade.model.SendCtraderEntity;
import luyen.tradebot.Trade.repository.AlertTradingRepository;
import luyen.tradebot.Trade.repository.ConnectRepository;
import luyen.tradebot.Trade.repository.SendCtraderRepository;
import luyen.tradebot.Trade.service.TracingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TracingServiceImpl implements TracingService {

    private final ConnectRepository connectRepository;

    private final AlertTradingRepository alertTradingRepository;

    private final SendCtraderRepository sendCtraderRepository;

    @Override
    public List<ConnectedEntity> getListConnected() {
        return connectRepository.findAll();
    }

    @Override
    public List<AlertTradingEntity> getListAlertTrading() {
        return alertTradingRepository.findAll();
    }

    @Override
    public List<SendCtraderEntity> getListSendCtrader() {
        return sendCtraderRepository.findAll();
    }
}
