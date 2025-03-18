package luyen.tradebot.Trade.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.AccountRequestDTO;
import luyen.tradebot.Trade.dto.request.BotRequestDTO;
import luyen.tradebot.Trade.dto.respone.BotResponse;
import luyen.tradebot.Trade.dto.respone.PageResponse;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.BotEntity;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.repository.BotRepository;
import luyen.tradebot.Trade.repository.UserRepository;
import luyen.tradebot.Trade.service.BotService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j(topic = "BOT-SERVICE")
@RequiredArgsConstructor()
public class BotServiceImpl implements BotService {

    private final BotRepository botRepository;

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public List<BotEntity> fillAll() {
        return List.of();
    }

    @Override
    public long saveBot(BotRequestDTO bot) {
       BotEntity botEntity = botRepository.save(BotEntity.builder()
                .botName(bot.getBotName())
                .botFrom(bot.getBotFrom())
                .status(bot.getBotStatus())
                .exchange(bot.getExchange())
                .maxAccount(bot.getMaxAccount())
                .description(bot.getDescription())
                .signalToken(bot.getSignalToken())
                .build());
        return botEntity.getId();
    }

    @Override
    public void deleteBot(long idBot) {
        botRepository.deleteById(idBot);
        log.info("Bot has deleted Successfully");
    }

    @Override
    public void updateBot(long id, BotRequestDTO bot) {
        BotEntity botEntity = botRepository.findById(id).orElseThrow(() -> new RuntimeException("Bot not found"));
        botEntity.setBotName(bot.getBotName());
        botEntity.setBotFrom(bot.getBotFrom());
        botEntity.setStatus(bot.getBotStatus());
        botEntity.setExchange(bot.getExchange());
        botEntity.setMaxAccount(bot.getMaxAccount());
        botEntity.setDescription(bot.getDescription());
        botEntity.setSignalToken(bot.getSignalToken());
        botRepository.save(botEntity);

    }

    @Override
    public long saveAccount(long id, AccountRequestDTO account) {
        BotEntity bot = botRepository.findById(id).orElseThrow(() -> new RuntimeException("Bot not found"));
       AccountEntity accountEntity = accountRepository.save(AccountEntity.builder()
                .bot(bot)
                .accountName(account.getName())
                .accessToken(account.getAccessToken())
                .clientId(account.getClientId())
                .ctidTraderAccountId(account.getCtidTraderAccountId())
                .exprixeDate(account.getExpirationDate())
                .typeAccount(account.getTypeAccount())
                .secretId(account.getSecretId())
                .status(account.getStatus())
                .build());
        return accountEntity.getId();
    }

    @Override
    public BotResponse getBotById(long id) {
        BotEntity botEntity = botRepository.findById(id).orElseThrow(() -> new RuntimeException("Bot not found"));
        return BotResponse.builder()
                .id(id)
                .botFrom(botEntity.getBotFrom())
                .botName(botEntity.getBotName())
                .signalToken(botEntity.getSignalToken())
                .exchange(botEntity.getExchange())
                .maxAccount(botEntity.getMaxAccount())
                .description(botEntity.getDescription())
                .numberAccount(botEntity.getNumberAccount())
                .createDate(botEntity.getCreateAt())
                .build();
    }

    @Override
    public PageResponse<BotEntity> getAllBots(int pageNo, int pageSize, String sortBy) {
        return null;
    }

    private BotEntity getBotById(String id) {
        return null;
    }
}
