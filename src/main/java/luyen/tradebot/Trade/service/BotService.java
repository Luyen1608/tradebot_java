package luyen.tradebot.Trade.service;

import luyen.tradebot.Trade.dto.request.BotRequestDTO;
import luyen.tradebot.Trade.dto.respone.PageResponse;
import luyen.tradebot.Trade.model.BotEntity;
import org.springframework.stereotype.Service;

import java.util.List;


public interface BotService {

    List<BotEntity> fillAll();

    long saveBot(BotRequestDTO bot);

    void deleteBot(BotEntity bot);

    void updateBot(BotEntity bot);

    BotEntity getBotById(long id);

    PageResponse<BotEntity> getAllBots(int pageNo, int pageSize, String sortBy);

}
