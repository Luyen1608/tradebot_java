package luyen.tradebot.Trade.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.BotRequestDTO;
import luyen.tradebot.Trade.dto.respone.PageResponse;
import luyen.tradebot.Trade.model.BotEntity;
import luyen.tradebot.Trade.repository.BotRepository;
import luyen.tradebot.Trade.service.BotService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j(topic = "BOT-SERVICE")
@RequiredArgsConstructor()
public class BotServiceImpl implements BotService {

    private final BotRepository botRepository;

    @Override
    public List<BotEntity> fillAll() {
        return List.of();
    }

    @Override
    public long saveBot(BotRequestDTO bot) {
        botRepository.save(BotEntity.builder()
                .botName(bot.getBotName())
                .botFrom(bot.getBotFrom())
                .status(bot.getBotStatus())
                .exchange(bot.getExchange())
                .maxAccount(bot.getMaxAccount())
                .description(bot.getDescription())
                .signalToken(bot.getSignalToken())
                .build());
        return 0;
    }

    @Override
    public void deleteBot(BotEntity bot) {

    }

    @Override
    public void updateBot(BotEntity bot) {

    }

    @Override
    public BotEntity getBotById(long id) {
        return null;
    }

    @Override
    public PageResponse<BotEntity> getAllBots(int pageNo, int pageSize, String sortBy) {
        return null;
    }
}
