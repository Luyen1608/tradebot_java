package luyen.tradebot.Trade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for sending messages to Kafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Send a message to a Kafka topic
     * @param topic The topic to send the message to
     * @param key The key for the message
     * @param value The value object to be serialized to JSON
     * @return CompletableFuture of the send result
     */
//    public CompletableFuture<SendResult<String, String>> sendMessage(String topic, String key, Object value) {
//        try {
//            String jsonValue = objectMapper.writeValueAsString(value);
//            log.info("Sending message to topic {}: key={}, value={}", topic, key, jsonValue);
//            return kafkaTemplate.send(topic, key, jsonValue)
//                    .whenComplete((result, ex) -> {
//                        if (ex == null) {
//                            log.info("Message sent successfully to topic {}: key={}, offset={}",
//                                    topic, key, result.getRecordMetadata().offset());
//                        } else {
//                            log.error("Failed to send message to topic {}: key={}", topic, key, ex);
//                        }
//                    });
//        } catch (JsonProcessingException e) {
//            log.error("Error serializing message: {}", e.getMessage());
//            CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
//            future.completeExceptionally(e);
//            return future;
//        }
//    }
    public CompletableFuture<SendResult<String, String>> sendMessage(String topic, String key, Object value) {
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            log.info("Preparing to send message to topic '{}': key='{}', value='{}'", topic, key, jsonValue);
            kafkaTemplate.send(topic, key, jsonValue).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message sent successfully to topic '{}': key='{}', partition={}, offset={}",
                            topic, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    future.complete(result);
                } else {
                    log.error("Failed to send message to topic '{}': key='{}', error='{}'", topic, key, ex.getMessage());
                    future.completeExceptionally(ex);
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Error serializing message for topic '{}': key='{}', error='{}'", topic, key, e.getMessage());
            future.completeExceptionally(e);
        } catch (Exception e) {
            log.error("Unexpected error while sending message to topic '{}': key='{}', error='{}'", topic, key, e.getMessage());
            future.completeExceptionally(e);
        }
        return future;
    }

}