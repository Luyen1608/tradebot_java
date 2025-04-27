package luyen.tradebot.Trade.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuration for Kafka topics
 */
@Configuration
@ConditionalOnProperty(name = "enable.kafka", havingValue = "true")
public class KafkaTopicConfig {

//    /**
//     * Create the Confirm-Account-Topic
//     *
//     * @return The topic configuration
//     */
//    @Bean
//    public NewTopic confirmAccountTopic() {
//        return TopicBuilder.name("Confirm-Account-Topic")
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
//
//    /**
//     * Create the Order-Topic
//     *
//     * @return The topic configuration
//     */
//    @Bean
//    public NewTopic orderTopic() {
//        return TopicBuilder.name("Order-Topic")
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
//
//    /**
//     * +     * Create the Order-Status-Topic
//     * +     * @return The topic configuration
//     * +
//     */
//    @Bean
//    public NewTopic orderStatusTopic() {
//        return TopicBuilder.name("order-status-topic")
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
}