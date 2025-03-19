package luyen.tradebot.Trade.service;

import luyen.tradebot.Trade.model.AlertTradingEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import luyen.tradebot.Trade.model.SendCtraderEntity;

import java.util.List;

public interface TracingService {

    List<ConnectedEntity> getListConnected();

    List<AlertTradingEntity> getListAlertTrading();

    List<SendCtraderEntity> getListSendCtrader();

}
