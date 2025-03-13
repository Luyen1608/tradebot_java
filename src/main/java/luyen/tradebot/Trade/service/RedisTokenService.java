package luyen.tradebot.Trade.service;

import lombok.RequiredArgsConstructor;
import luyen.tradebot.Trade.model.RedisToken;
import luyen.tradebot.Trade.repository.RedisTokenRepository;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTokenRepository redisTokenRepository;

    public String save(RedisToken redisToken) {
        RedisToken result = redisTokenRepository.save(redisToken);

        return result.getId();
    }
    public void delete(String id ) {
        redisTokenRepository.deleteById(id);
    }
}
