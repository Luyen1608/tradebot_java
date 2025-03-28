package luyen.tradebot.Trade.service;

import luyen.tradebot.Trade.dto.request.AccountRequestDTO;
import luyen.tradebot.Trade.dto.request.BotRequestDTO;
import luyen.tradebot.Trade.dto.respone.BotResponse;
import luyen.tradebot.Trade.dto.respone.PageResponse;
import luyen.tradebot.Trade.model.BotEntity;
import org.springframework.stereotype.Service;

import java.util.List;


public interface BotService {

    List<BotEntity> fillAll();

    long saveBot(BotRequestDTO bot);

    void deleteBot(long id);

    long updateBot(long id, BotRequestDTO bot);

    long saveAccount(long id, AccountRequestDTO account);

    BotResponse getBotById(long id);

    PageResponse<BotEntity> getAllBots(int pageNo, int pageSize, String sortBy);

    BotEntity createBot(BotRequestDTO botDTO);

    BotEntity getBot(Long id);

    List<BotEntity> getAllBots();

    List<BotEntity> getActiveBots();

}
