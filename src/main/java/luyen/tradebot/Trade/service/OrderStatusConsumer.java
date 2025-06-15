package luyen.tradebot.Trade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.ErrorLogEntity;
import luyen.tradebot.Trade.model.OrderEntity;
import luyen.tradebot.Trade.model.OrderPosition;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.repository.OrderPositionRepository;
import luyen.tradebot.Trade.repository.OrderRepository;
import luyen.tradebot.Trade.repository.impl.ErrorLogRepository;
import luyen.tradebot.Trade.util.ValidateRepsone;
import luyen.tradebot.Trade.util.enumTraderBot.ActionSystem;
import luyen.tradebot.Trade.util.enumTraderBot.PayloadType;
import luyen.tradebot.Trade.util.enumTraderBot.ProtoOAErrorCode;
import luyen.tradebot.Trade.util.enumTraderBot.ProtoOAExecutionType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    private final ErrorLogRepository errorLogRepository;
    private final OrderPositionRepository orderPositionRepository;
    private final ValidateRepsone validateRepsone;

    private final SignalAccountStatusService signalAccountStatusService;

    /**
     * Listener for the order-status-topic
     *
     * @param message The message received from Kafka
     */
    @KafkaListener(topics = "order-status-topic", groupId = "tradebot-group")
    public void listenOrderStatus(String message) {
//        log.info("Received message from order-status-topic: {}", message);
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

    @KafkaListener(topics = "order-status-auth", groupId = "tradebot-group")
    public void listenAuthStatus(String message) {
//        log.info("Received message from order-auth-topic: {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            // Lấy thông tin từ message
            String rawMessage = jsonNode.path("rawMessage").asText();
            String accountId = jsonNode.path("accountId").asText();
            String messageType = jsonNode.path("messageType").asText();
            String clientMsgId = jsonNode.path("clientMsgId").asText();
            // Parse raw message để lấy thông tin chi tiết
            JsonNode rawMessageNode = objectMapper.readTree(rawMessage);
            ResponseCtraderDTO res = ValidateRepsone.formatResponse(rawMessage);

            AccountEntity account = accountRepository.findById(UUID.fromString(accountId))
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            // Xử lý message dựa trên loại
            if (!res.getErrorCode().equals("N/A")){
                account.setErrorMessage(res.getErrorCode() + "-" + res.getDescription());
//                ProtoOAErrorCode protoOAErrorCode = ProtoOAErrorCode.valueOf(res.getErrorCode());
                account.setIsAuthenticated(false);
            }
            accountRepository.save(account);
        } catch (JsonProcessingException e) {
            log.error("Error parsing message: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
        }
    }
    @KafkaListener(topics = "write-log-error", groupId = "tradebot-group")
    public void addErrorLog(String message) {
        log.info("Received message from write-log-error: {}", message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            // Lấy thông tin từ message
            String rawMessage = jsonNode.path("rawMessage").asText();
            String accountId = jsonNode.path("accountId").asText();
            String messageType = jsonNode.path("messageType").asText();
            String clientMsgId = jsonNode.path("clientMsgId").asText();
            // Parse raw message để lấy thông tin chi tiết
            JsonNode rawMessageNode = objectMapper.readTree(rawMessage);
            ResponseCtraderDTO res = ValidateRepsone.formatResponse(rawMessage);
            ErrorLogEntity errorLogEntity = ErrorLogEntity.builder()
                    .accountId(accountId)
                    .errorMessage(rawMessage)
                    .build();
            errorLogRepository.save(errorLogEntity);
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

            // Get account information
            AccountEntity account = accountRepository.findById(UUID.fromString(accountId))
                    .orElseThrow(() -> new RuntimeException("Account not found with accountId Order Position "));

            try {

                OrderPosition orderPosition = orderPositionRepository.findByClientMsgId(clientMsgId)
                        .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgId));

                orderPosition.setVolumeMultiplier(account.getVolumeMultiplier());
                orderPosition.setVolumeSent(jsonNode.path("payload").path("volume").asInt());
                orderPosition.setStatus(ProtoOAExecutionType.ORDER_ACCEPTED.getStatus());
                orderPositionRepository.saveAndFlush(orderPosition);
//                // First check if the order position exists
//                OrderPosition orderPosition = orderPositionRepository.findByClientMsgId(clientMsgId)
//                        .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgId));
//
//                // Store the ID for later use
//                final UUID orderPositionId = orderPosition.getId();
//
//                // Use direct update method to avoid optimistic locking issues
//                int updatedRows = orderPositionRepository.updateByOrderCtraderIdAndPositionId(
//                        null, // executionType - not changing
//                        null, // errorMessage
//                        null, // errorCode
//                        ProtoOAExecutionType.ORDER_ACCEPTED.getStatus(),
//                        clientMsgId
//                );
//
//                if (updatedRows > 0) {
//                    // Refresh the entity to get the latest state
//                    orderPosition = orderPositionRepository.findById(orderPositionId)
//                            .orElseThrow(() -> new RuntimeException("OrderPosition not found after update"));
//
//                    // Update additional fields
//                    orderPosition.setVolumeMultiplier(account.getVolumeMultiplier());
//                    orderPosition.setVolumeSent(jsonNode.path("payload").path("volume").asInt());
//                    orderPositionRepository.saveAndFlush(orderPosition);
//                }
            } catch (Exception e) {
                log.error("createNewOrder: {}", e.getMessage());
                throw e;
            }
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
            log.info("Process Order Status Message kafka");
            // Convert MessageType to PayloadType enum using its value
            PayloadType payloadType = PayloadType.valueOf(messageType);

            switch (payloadType) {
                case PROTO_OA_CLOSE_POSITION_REQ:
                    processCloseOrderReq(jsonNode, accountId, clientMsgId);
                    break;
                case PROTO_OA_EXECUTION_EVENT:
                    processNewOrderReq(jsonNode, accountId, clientMsgId);
                    break;
                case PROTO_OA_ORDER_ERROR_EVENT:
                case PROTO_OA_ERROR_RES:
                    processErrorEvent(jsonNode, accountId, clientMsgId);
                    break;
                default:
                    log.info("Message type {} not handled for database pers istence", messageType);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Unknown message type: {}", messageType);
        }
    }

    @Transactional
    public void processCloseOrderReq(JsonNode jsonNode, UUID accountId, String clientMsgId) {
        log.info("Process Close Order Req kafka");
        ResponseCtraderDTO responseCtraderDTO = validateRepsone.formatResponsePlaceOrder(jsonNode.toString());

        // First, get the OrderPosition to check if it exists and get necessary data
        OrderPosition orderPosition = orderPositionRepository.findByClientMsgIdLimitOne(clientMsgId)
                .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgId));

        // Store the ID for later use
        final UUID orderPositionId = orderPosition.getId();

        ProtoOAExecutionType executionType = ProtoOAExecutionType.fromCode(responseCtraderDTO.getExecutionType());
        switch (executionType) {
            case ORDER_ACCEPTED:
                try {
                    orderPosition.setOrderCtraderId(responseCtraderDTO.getOrderCtraderId());
                    orderPosition.setVolumeSent(responseCtraderDTO.getVolume());
                    orderPosition.setPositionId(responseCtraderDTO.getPositionId());
                    orderPosition.setStatus(ProtoOAExecutionType.ORDER_ACCEPTED.getStatus());
                    orderPosition.setExecutionType(ProtoOAExecutionType.ORDER_ACCEPTED.toString());
                } catch (Exception e) {
                    log.error("Error processing ORDER_ACCEPTED close order request: {}", e.getMessage(), e);
                    // Re-throw to ensure transaction is rolled back
                    throw new RuntimeException("Failed to process close order request", e);
                }
//
//                // Use repository method to update directly in the database
//                int updatedRows = orderPositionRepository.updateByOrderCtraderIdAndPositionId(
//                        ProtoOAExecutionType.ORDER_ACCEPTED.toString(),
//                        null, // errorMessage
//                        null, // errorCode
//                        ProtoOAExecutionType.ORDER_ACCEPTED.getStatus(),
//                        clientMsgId
//                );
//
//                // Additional updates that aren't covered by the repository method
//                if (updatedRows > 0) {
//                    // Refresh the entity to get the latest state
//                    orderPosition = orderPositionRepository.findById(orderPositionId)
//                            .orElseThrow(() -> new RuntimeException("OrderPosition not found after update"));
//
//                    orderPosition.setOrderCtraderId(responseCtraderDTO.getOrderCtraderId());
//                    orderPosition.setVolumeSent(responseCtraderDTO.getVolume());
//                    orderPosition.setPositionId(responseCtraderDTO.getPositionId());
//                    orderPositionRepository.saveAndFlush(orderPosition);
//                }
                break;

            case ORDER_FILLED:
                try {
                    orderPosition.setStatus(ProtoOAExecutionType.ORDER_FILLED.getStatus());
                    orderPosition.setExecutionType(ProtoOAExecutionType.ORDER_FILLED.toString());
                    //if success close order then update order entity
                    OrderEntity orderEntity = orderRepository.findById(orderPosition.getOrder().getId()).orElseThrow(
                            () -> new RuntimeException("Order not found with orderId: " + orderPosition.getOrder().getId()));
                    orderEntity.setStatus(ProtoOAExecutionType.ORDER_CLOSE.getStatus());
                    orderRepository.saveAndFlush(orderEntity);
                } catch (Exception e) {
                    log.error("Error processing ORDER_FILLED close order request: {}", e.getMessage(), e);
                    // Re-throw to ensure transaction is rolled back
                    throw new RuntimeException("Failed to process close order request", e);
                }


//
//                // Update order position status directly
//                updatedRows = orderPositionRepository.updateByOrderCtraderIdAndPositionId(
//                        ProtoOAExecutionType.ORDER_FILLED.toString(),
//                        null, // errorMessage
//                        null, // errorCode
//                        ProtoOAExecutionType.ORDER_FILLED.getStatus(),
//                        clientMsgId
//                );
//
//                // Handle order entity update if needed
//                if (updatedRows > 0 && orderPosition.getOrder() != null) {
//                    UUID orderId = orderPosition.getOrder().getId();
//                    // Update order entity in a separate transaction to avoid locking issues
//                    OrderEntity orderEntity = orderRepository.findById(orderId)
//                            .orElseThrow(() -> new RuntimeException("Order not found with orderId: " + orderId));
//                    orderEntity.setStatus(ProtoOAExecutionType.ORDER_CLOSE.getStatus());
//                    orderRepository.saveAndFlush(orderEntity);
//                    log.info("Order status updated to CLOSE for orderId: {}", orderId);
//                }
                break;
            default:
                log.info("Execution type {} not handled for database persistence", jsonNode.path("payload").path("executionType"));
        }
        orderPositionRepository.saveAndFlush(orderPosition);
    }

    @Transactional
    public void processNewOrderReq(JsonNode jsonNode, UUID accountId, String clientMsgId) {
        try {
            log.info("Process New Order Req kafka");
            ResponseCtraderDTO responseCtraderDTO = validateRepsone.formatResponsePlaceOrder(jsonNode.toString());
            ProtoOAExecutionType executionType = ProtoOAExecutionType.fromCode(responseCtraderDTO.getExecutionType());
            OrderPosition orderPosition = orderPositionRepository.findByClientMsgIdLimitOne(clientMsgId)
                    .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgId));

            // Store necessary data before any updates
            final UUID orderPositionId = orderPosition.getId();
            final String tradeSide = orderPosition.getTradeSide();
            final String symbol = orderPosition.getSymbol();
            final String ctidTraderAccountId = orderPosition.getCtidTraderAccountId();

            switch (executionType) {
                case ORDER_ACCEPTED:
                    log.info("Process New Order Req kafka Execution Type Accepted");
                    orderPosition.setOrderCtraderId(responseCtraderDTO.getOrderCtraderId());
                    orderPosition.setVolumeSent(responseCtraderDTO.getVolume());
                    orderPosition.setPositionId(responseCtraderDTO.getPositionId());
                    orderPosition.setStatus(ProtoOAExecutionType.ORDER_ACCEPTED.getStatus());
                    orderPosition.setExecutionType(ProtoOAExecutionType.ORDER_ACCEPTED.toString());
                    orderPositionRepository.saveAndFlush(orderPosition);
                    break;
                case ORDER_FILLED:
                    log.info("Process New Order Req kafka Execution Type Filled for orderPosition: {}", orderPositionId);
                    // Update order position status directly
                    try {
                        int updatedRows = orderPositionRepository.updateByOrderCtraderIdAndPositionId(
                                ProtoOAExecutionType.ORDER_FILLED.toString(),
                                null, // errorMessage
                                null, // errorCode
                                "OPEN",
                                clientMsgId
                        );
                        log.info("Update result: {} rows affected for clientMsgId: {}", updatedRows, clientMsgId);

                        if (updatedRows == 0) {
                            // Fallback to direct entity update if the query didn't update any rows
                            log.warn("No rows updated by query, falling back to direct entity update");
                            orderPosition.setExecutionType(ProtoOAExecutionType.ORDER_FILLED.toString());
                            orderPosition.setStatus(ProtoOAExecutionType.ORDER_FILLED.getStatus());
                            orderPositionRepository.saveAndFlush(orderPosition);
                            log.info("Direct entity update completed for orderPosition: {}", orderPositionId);
                        }
                    } catch (Exception e) {
                        log.error("Error updating order position: {}", e.getMessage(), e);
                        // Fallback to direct entity update
                        orderPosition.setExecutionType(ProtoOAExecutionType.ORDER_FILLED.toString());
                        orderPosition.setStatus(ProtoOAExecutionType.ORDER_FILLED.getStatus());
                        orderPositionRepository.saveAndFlush(orderPosition);
                        log.info("Fallback direct entity update completed after error");
                    }
                    // Handle closing order if needed
                    if (responseCtraderDTO.isClosingOrder() && orderPosition.getOrder() != null) {
                        UUID orderId = orderPosition.getOrder().getId();
                        log.info("Processing closing order for orderId: {}", orderId);

                        // Update order entity in a separate transaction to avoid locking issues
                        OrderEntity orderEntity = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found with orderId: " + orderId));
                        orderEntity.setStatus(ProtoOAExecutionType.ORDER_CLOSE.getStatus());
                        orderRepository.saveAndFlush(orderEntity);
                        log.info("Order status updated to CLOSE for orderId: {}", orderId);
                    }
                    break;

                default:
                    log.info("Execution type {} not handled for database persistence", jsonNode.path("payload").path("executionType"));
            }

            // Ensure transaction is committed before running async task
            final String clientMsgIdFinal = clientMsgId;
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("Starting async task for orderPositionId: {}", orderPositionId);
                    // Fetch a fresh instance from the database to ensure we have the latest state
                    OrderPosition detachedOrderPosition = orderPositionRepository.findById(orderPositionId)
                            .orElseGet(() -> {
                                // Fallback to find by clientMsgId if ID lookup fails
                                log.info("Falling back to clientMsgId lookup");
                                return orderPositionRepository.findByClientMsgIdLimitOne(clientMsgIdFinal)
                                        .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgIdFinal));
                            });

                    log.info("Fetched detached OrderPosition with status: {}", detachedOrderPosition.getStatus());

                    signalAccountStatusService.sendSignalAccountStatus(detachedOrderPosition,
                            tradeSide, symbol, ctidTraderAccountId);
                    log.info("Signal account status sent successfully");
                } catch (Exception e) {
                    log.error("Error sending signal account status: {}", e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            log.error("Error processing new order request: {}", e.getMessage(), e);
            // Re-throw to ensure transaction is rolled back
            throw new RuntimeException("Failed to process new order request", e);
        }
    }


    /**
     * Process error event and save to database
     *
     * @param jsonNode    The JSON message
     * @param accountId   The account ID
     * @param clientMsgId The client message ID
     */
    @Transactional
    public void processErrorEvent(JsonNode jsonNode, UUID accountId, String clientMsgId) {
        try {
            String errorCode = jsonNode.path("payload").has("errorCode") ?
                    jsonNode.path("payload").get("errorCode").asText(null) : null;
            String errorDescription = jsonNode.path("payload").has("description") ?
                    jsonNode.path("payload").get("description").asText(null) : null;

            // First, get the OrderPosition to check if it exists and get necessary data
            OrderPosition orderPosition = orderPositionRepository.findByClientMsgIdLimitOne(clientMsgId)
                    .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgId));

            // Store necessary data before any updates
            final UUID orderPositionId = orderPosition.getId();
            final String tradeSide = orderPosition.getTradeSide();
            final String symbol = orderPosition.getSymbol();
            final String ctidTraderAccountId = orderPosition.getCtidTraderAccountId();

            // Use direct update method to avoid optimistic locking issues
            orderPositionRepository.updateErrorCodeAndErrorMessageByClientMsgId(
                    errorCode,
                    errorDescription,
                    ProtoOAExecutionType.ORDER_REJECTED.getStatus(),
                    clientMsgId
            );

            log.info("Updated error event in database: clientMsgId={}, errorCode={}",
                    clientMsgId, errorCode);

            // Ensure transaction is committed before running async task
            CompletableFuture.runAsync(() -> {
                try {
                    // Fetch a fresh instance from the database to ensure we have the latest state
                    OrderPosition detachedOrderPosition = orderPositionRepository.findById(orderPositionId)
                            .orElseThrow(() -> new RuntimeException("OrderPosition not found with ID: " + orderPositionId));

                    signalAccountStatusService.sendSignalAccountStatus(detachedOrderPosition,
                            tradeSide, symbol, ctidTraderAccountId);
                } catch (Exception e) {
                    log.error("Error sending signal account status: {}", e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            log.error("Error saving error event to database: {}", e.getMessage(), e);
        }
    }
}