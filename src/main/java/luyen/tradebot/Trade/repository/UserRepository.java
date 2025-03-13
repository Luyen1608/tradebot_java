package luyen.tradebot.Trade.repository;

//import luyen.tradebot.Trade.model.UserEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;

import luyen.tradebot.Trade.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
}

