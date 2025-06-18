package luyen.tradebot.Trade.repository;

import luyen.tradebot.Trade.model.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    List<AccountEntity> findByIsActive(boolean isActive);
    //get list account active
    @Query("SELECT a FROM Account a WHERE a.isActive = ?1 AND a.isAuthenticated = ?2")
    List<AccountEntity> findByIsActiveAndAuthenticated(boolean isActive, boolean isAuthenticated);

    List<AccountEntity> findByBotId(UUID botId);

    @Query("SELECT a FROM Account a WHERE a.isActive = true AND a.isConnected = false")
    List<AccountEntity> findActiveDisconnectedAccounts();

    @Query("SELECT a FROM Account a WHERE a.bot.id = ?1 AND a.isActive = true AND a.isAuthenticated = true")
    List<AccountEntity> findByBotIdAndIsActiveAndIsAuthenticated(UUID botId, boolean isActive, boolean isAuthenticated);


    boolean existsByClientId(String clientId);

    boolean existsByAccessToken(String accessToken);

    // delete account by id
    void deleteById(UUID id);

    Optional<AccountEntity> findById(UUID id);

    //update isactive

@Modifying
@Query("UPDATE Account a SET a.isActive = :isActive WHERE a.id = :accountId")
void updateIsActiveById(boolean isActive, UUID accountId);
    //find account by id

}
