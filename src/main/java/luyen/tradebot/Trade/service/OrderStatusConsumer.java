package luyen.tradebot.Trade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.OrderPosition;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.repository.OrderPositionRepository;
import luyen.tradebot.Trade.repository.OrderRepository;
import luyen.tradebot.Trade.util.ValidateRepsone;
import luyen.tradebot.Trade.util.enumTraderBot.PayloadType;
import luyen.tradebot.Trade.util.enumTraderBot.ProtoOAExecutionType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for consuming order status messages from Kafka and saving to database
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderStatusConsumer {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final OrderPositionRepository orderPositionRepository;
    private final ValidateRepsone validateRepsone;

    /**
     * Listener for the order-status-topic
     *
     * @param message The message received from Kafka
     */
    @KafkaListener(topics = "order-status-topic", groupId = "tradebot-group")
    @Transactional
    public void listenOrderStatus(String message) {
        log.info("Received message from order-status-topic: {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            // Lấy thông tin từ message
            String rawMessage = jsonNode.path("rawMessage").asText();
            String accountId = jsonNode.path("accountId").asText();
            String messageType = jsonNode.path("messageType").asText();
            String clientMsgId = jsonNode.path("clientMsgId").asText();

            // Parse raw message để lấy thông tin chi tiết
            JsonNode rawMessageNode = objectMapper.readTree(rawMessage);

            // Xử lý message dựa trên loại
            processOrderStatusMessage(rawMessageNode, UUID.fromString(accountId), messageType, clientMsgId);

        } catch (JsonProcessingException e) {
            log.error("Error parsing message: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-placed-topic", groupId = "tradebot-group")
    @Transactional
    public void createNewOrder(String message) {
        log.info("Received message from order-placed-topic: {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            //       {
            //   "accountId":"01f2c7a6-7701-43e4-84b1-0dc86c5a14d9",
            //   "clientMsgId":"trade365_e74ee564",
            //   "rawMessage":"{\"clientMsgId\": \"trade365_e74ee564\",\"payloadType\": 2106,\"payload\": {\"ctidTraderAccountId\": 43196577,\"symbolId\": 101,\"tradeSide\": 1,\"orderType\": 1,\"volume\": 8}}",
            //   "timestamp":1745756326068
            //}
            // Lấy thông tin từ message
            String rawMessage = jsonNode.path("rawMessage").asText();
            String accountId = jsonNode.path("accountId").asText();
            String clientMsgId = jsonNode.path("clientMsgId").asText();
            // Parse raw message để lấy thông tin chi tiết
            JsonNode rawMessageNode = objectMapper.readTree(rawMessage);
            // Xử lý save OrderPosition
            AccountEntity account = accountRepository.findById(UUID.fromString(accountId))
                    .orElseThrow(() -> new RuntimeException("Account not found with accountId Order Position "));
//        BotsEntity bot = botsRepository.findBySignalToken(webhookDTO.getSignalToken())
//                .orElseThrow(() -> new RuntimeException("Bot not found with signal token: " + webhookDTO.getSignalToken()));
            //get orderposition by clientMsgId
            OrderPosition orderPosition = orderPositionRepository.findByClientMsgId(clientMsgId)
                    .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgId));

            orderPosition.setVolumeMultiplier(account.getVolumeMultiplier());
            orderPosition.setVolumeSent(jsonNode.path("payload").path("volume").asInt());
//            orderPosition.setPositionId(jsonNode.path("payload").path("position").path("positionId").asInt());
            orderPosition.setStatus(ProtoOAExecutionType.ORDER_ACCEPTED.getStatus());
            orderPositionRepository.saveAndFlush(orderPosition);
        } catch (JsonProcessingException e) {
            log.error("Error parsing message: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
        }
    }

    /**
     * Process the order status message based on message type
     *
     * @param jsonNode    The JSON message
     * @param accountId   The account ID
     * @param messageType The message type
     * @param clientMsgId The client message ID
     */
    private void processOrderStatusMessage(JsonNode jsonNode, UUID accountId, String messageType, String clientMsgId) {
        try {
            PayloadType payloadType = PayloadType.valueOf(messageType);

            switch (payloadType) {
                case PROTO_OA_EXECUTION_EVENT:
                    processNewOrderReq(jsonNode, accountId, clientMsgId);
                    break;
                case PROTO_OA_ORDER_ERROR_EVENT:
                case PROTO_OA_ERROR_RES:
                    processErrorEvent(jsonNode, accountId, clientMsgId);
                    break;
                default:
                    log.info("Message type {} not handled for database persistence", messageType);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Unknown message type: {}", messageType);
        }
    }

    private void processNewOrderReq(JsonNode jsonNode, UUID accountId, String clientMsgId) {
        ResponseCtraderDTO responseCtraderDTO = validateRepsone.formatResponsePlaceOrder(jsonNode.toString());
        //get account by accountId
        OrderPosition orderPosition = orderPositionRepository.findByClientMsgId(clientMsgId)
                .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgId));
        switch (ProtoOAExecutionType.fromCode(responseCtraderDTO.getExecutionType())) {
            case ORDER_ACCEPTED:
                orderPosition.setOrderCtraderId(responseCtraderDTO.getOrderCtraderId());
                orderPosition.setVolumeSent(responseCtraderDTO.getVolume());
                orderPosition.setPositionId(responseCtraderDTO.getPositionId());
                orderPosition.setStatus(ProtoOAExecutionType.ORDER_ACCEPTED.getStatus());
                break;
            case ORDER_FILLED:
                orderPosition.setStatus(ProtoOAExecutionType.ORDER_FILLED.getStatus());
                break;
            default:
                log.info("Execution type {} not handled for database persistence", jsonNode.path("payload").path("executionType"));
        }
        orderPositionRepository.saveAndFlush(orderPosition);
    }

    /**
     * Process execution event and save to database
     *
     * @param jsonNode    The JSON message
     * @param accountId   The account ID
     * @param clientMsgId The client message ID
     */
    private void processExecutionEvent(JsonNode jsonNode, UUID accountId, String clientMsgId) {
        try {
            int executionType = jsonNode.path("payload").path("executionType").asInt();
            int positionId = jsonNode.path("payload").path("position").path("positionId").asInt();
            String orderStatus = ProtoOAExecutionType.fromCode(executionType).getStatus();

            OrderPosition orderPosition = orderPositionRepository.findByClientMsgId(clientMsgId)
                    .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgId));
            //convert jsonNode to string
            String jsonString = jsonNode.toString();
            ResponseCtraderDTO responseCtraderDTO = validateRepsone.formatResponsePlaceOrder(jsonString);
            if (responseCtraderDTO.getExecutionType() == ProtoOAExecutionType.ORDER_ACCEPTED.getCode()) {
               //setting toàn bộ value in orderPosition
                log.info("ExecutionEvent ORDER_ACCEPTED(2)");
                ProtoOAExecutionType executionTypeEnum = ProtoOAExecutionType.fromCode(responseCtraderDTO.getExecutionType());
                PayloadType payloadType = PayloadType.fromValue(responseCtraderDTO.getPayloadReponse());

                orderPosition.setPayloadType(payloadType.toString());
                orderPosition.setExecutionType(executionTypeEnum.getDescription());
                orderPosition.setOrderCtraderId(responseCtraderDTO.getOrderCtraderId());
                orderPosition.setPositionId(responseCtraderDTO.getPositionId());
                orderPosition.setVolumeSent(responseCtraderDTO.getVolume());
                orderPosition.setOrderCtraderId(responseCtraderDTO.getOrderCtraderId());
                orderPosition.setClientMsgId(responseCtraderDTO.getClientMsgId());
            }

            orderPosition.setStatus(orderStatus);
            orderPositionRepository.saveAndFlush(orderPosition);

            log.info("Saved execution event to database: clientMsgId={}, positionId={}, status={}",
                    clientMsgId, positionId, orderStatus);
        } catch (Exception e) {
            log.error("Error saving execution event to database: {}", e.getMessage(), e);
        }
    }
    /**
     * Process error event and save to database
     *
     * @param jsonNode    The JSON message
     * @param accountId   The account ID
     * @param clientMsgId The client message ID
     */
    private void processErrorEvent(JsonNode jsonNode, UUID accountId, String clientMsgId) {
        try {
            String errorCode = jsonNode.path("payload").has("errorCode") ?
                    jsonNode.path("payload").get("errorCode").asText(null) : null;
            String errorDescription = jsonNode.path("payload").has("description") ?
                    jsonNode.path("payload").get("description").asText(null) : null;
            OrderPosition orderPosition = orderPositionRepository.findByClientMsgId(clientMsgId)
                    .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgId));
            orderPosition.setStatus(ProtoOAExecutionType.ORDER_REJECTED.getStatus());
            orderPosition.setErrorCode(errorCode);
            orderPosition.setErrorMessage(errorDescription);
            orderPositionRepository.saveAndFlush(orderPosition);
//            orderPositionRepository.updateErrorCodeAndErrorMessageByClientMsgId(
//                    errorCode,
//                    errorDescription,
//                    ProtoOAExecutionType.ORDER_REJECTED.getStatus(),
//                    clientMsgId);
            log.info("Updated error event in database: clientMsgId={}, errorCode={}",
                    clientMsgId, errorCode);

        } catch (Exception e) {
            log.error("Error saving error event to database: {}", e.getMessage(), e);
        }
    }
}