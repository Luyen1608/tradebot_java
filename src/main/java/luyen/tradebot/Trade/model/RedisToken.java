package luyen.tradebot.Trade.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
//import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@RedisHash("RedisToken")
public class RedisToken implements Serializable {

    private String id;
    private String accessToken;
    private String refreshToken;
    private String resetToken;
}
