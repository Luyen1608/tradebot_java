package luyen.tradebot.Trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = { HibernateJpaAutoConfiguration.class })
//@EnableJpaRepositories(basePackages = "luyen.tradebot.Trade")
public class TradeApplication {
	public static void main(String[] args) {
		SpringApplication.run(TradeApplication.class, args);
	}

}
