package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.BotsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BotsRepository extends JpaRepository<BotsEntity, UUID> {
    Optional<BotsEntity> findBySignalToken(String signalToken);

}