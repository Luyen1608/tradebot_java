package luyen.tradebot.Trade.service;

import luyen.tradebot.Trade.dto.request.AccountRequestDTO;
import luyen.tradebot.Trade.dto.request.BotRequestDTO;
import luyen.tradebot.Trade.dto.respone.BotResponse;
import luyen.tradebot.Trade.dto.respone.PageResponse;
import luyen.tradebot.Trade.model.BotEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


public interface BotService {

    List<BotEntity> fillAll();

    UUID saveBot(BotRequestDTO bot);

    void deleteBot(UUID id);

    UUID updateBot(UUID id, BotRequestDTO bot);

    UUID saveAccount(UUID id, AccountRequestDTO account);

    BotResponse getBotById(UUID id);

    PageResponse<BotEntity> getAllBots(int pageNo, int pageSize, String sortBy);

    BotEntity createBot(BotRequestDTO botDTO);

    BotEntity getBot(UUID id);

    List<BotEntity> getAllBots();

    List<BotEntity> getActiveBots();

}
