package luyen.tradebot.Trade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.MessageTradingViewDTO;
import luyen.tradebot.Trade.dto.OrderWebhookDTO;
import luyen.tradebot.Trade.dto.request.OrderDTO;
import luyen.tradebot.Trade.dto.request.PlaceOrderRequest;
import luyen.tradebot.Trade.dto.respone.OrderResponseCtrader;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;
import luyen.tradebot.Trade.model.*;
import luyen.tradebot.Trade.repository.*;
import luyen.tradebot.Trade.util.Convert;
import luyen.tradebot.Trade.util.SaveInfo;
import luyen.tradebot.Trade.util.ValidateRepsone;
import luyen.tradebot.Trade.util.enumTraderBot.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final BotsRepository botsRepository;
    private final OrderPositionRepository orderPositionRepository;
    private final CTraderConnectionService connectionService;
    private final AlertTradingRepository alertTradingRepository;
    private final SaveInfo saveInfo;
    private final CTraderApiService cTraderApiService;
    private final ValidateRepsone validateRepsone;
    private final AlertTradingService alertTradingService;

    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${tradebot.prefix}")
    private String prefix = "trade365_";

    public OrderEntity closeOrder(UUID orderId, UUID accountId) {
//        OrderEntity order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        AccountEntity account = accountRepository.findById(accountId)
//                .orElseThrow(() -> new RuntimeException("Account not found"));
//
//        OrderPosition position = orderPositionRepository.findByOrderIdAndAccountId(orderId, accountId)
//                .orElseThrow(() -> new RuntimeException("Position not found for this order and account"));
//
//        if (!"OPEN".equals(position.getStatus())) {
//            throw new RuntimeException("Position is not open");
//        }
//
//        if (!account.getConnecting().isConnected()) {
//            throw new RuntimeException("Account is not connected");
//        }
//
//        CTraderConnection connection = connectionService.getConnection(account.getId());
//        if (connection == null) {
//            position.setErrorMessage("No active connection for account");
//            orderPositionRepository.save(position);
//            return order;
//        }
//
//        CompletableFuture<String> future = connection.closePosition(position.getPositionId(), 1, 1);
//
//        future.thenAccept(result -> {
//            position.setStatus("CLOSED");
//            orderPositionRepository.save(position);
//
//            // Check if all positions are closed
//            List<OrderPosition> openPositions = orderPositionRepository.findByOrderId(orderId).stream()
//                    .filter(p -> "OPEN".equals(p.getStatus()))
//                    .toList();
//
//            if (openPositions.isEmpty()) {
//                order.setStatus("CLOSED");
//                order.setCloseTime(LocalDateTime.now());
//                orderRepository.save(order);
//            }
//        }).exceptionally(ex -> {
//            position.setStatus("ERROR_CLOSING");
//            position.setErrorMessage("Error closing: " + ex.getMessage());
//            orderPositionRepository.save(position);
//            return null;
//        });
//        return order;
        return null;
    }


    @Transactional
    public void processWebhookOrder(MessageTradingViewDTO messageTradingViewDTO) {

        OrderWebhookDTO webhookDTO = Convert.convertTradeviewToCtrader(messageTradingViewDTO);

        BotsEntity bot = botsRepository.findBySignalToken(webhookDTO.getSignalToken())
                .orElseThrow(() -> new RuntimeException("Bot not found with signal token: " + webhookDTO.getSignalToken()));

        List<AccountEntity> accounts = accountRepository.findByBotIdAndIsActiveAndIsAuthenticated(
                bot.getId(), true, true);

        if (accounts.isEmpty()) {
            log.warn("No active authenticated accounts found for bot: {}", bot.getId());
            return;
        }
        // Create a single order record
        OrderEntity order = OrderEntity.builder()
                .symbol(Symbol.fromId(webhookDTO.getSymbol()))
                .symbolId(Symbol.fromId(webhookDTO.getSymbol()).getId())
                .tradeSide(TradeSide.fromValue(webhookDTO.getTradeSide()))
                .volume(new BigDecimal(webhookDTO.getVolume()))
                .status("OPEN")
                .orderType(OrderType.fromValue(webhookDTO.getOrderType()))
                .comment("Created Order webhook for bot: " + bot.getName())
                .openTime(LocalDateTime.now())
                .account(accounts.get(0)) // Use the first account as the reference account
                .botId(bot.getId()) // Use the first account as the reference account
                .build();
        OrderEntity savedOrder = orderRepository.saveAndFlush(order);
        // Process order for each account in parallel
        for (AccountEntity account : accounts) {
            CompletableFuture<Void> accountFuture = CompletableFuture.runAsync(() -> {
                try {
                    CTraderConnection connection = connectionService.getConnection(account.getId());
                    if (connection == null) {
                        log.error("No active connection for account: {}", account.getId());
                        OrderPosition position = OrderPosition.builder()
                                .order(savedOrder)
                                .account(account)
                                .status("ERROR")
                                .errorMessage("No active connection for account")
                                .build();
                        orderPositionRepository.save(position);
                        return;
                    }
                    String clientMsgId = generateClientMsgId();
                    //save order
                    OrderPosition position = OrderPosition.builder()
                            .clientMsgId(clientMsgId)
                            .order(savedOrder)
                            .account(account)
                            .orderType("New_Order")
                            .status("PENDING")
                            .build();
                    OrderPosition savedPosition = orderPositionRepository.saveAndFlush(position);

                    // order placer
                    PlaceOrderRequest request = PlaceOrderRequest.builder()
                            .connection(connection)
                            .clientMsgId(clientMsgId)
                            .symbol(webhookDTO.getSymbol())
                            .tradeSide(webhookDTO.getTradeSide())
                            .volume(webhookDTO.getVolume())
                            .orderType(webhookDTO.getOrderType())
                            .account(account)
                            .savedOrder(savedOrder)
                            .build();
                    CompletableFuture<String> future = cTraderApiService.placeOrder(request);
                } catch (Exception e) {
                    log.error("Error processing order for account: {}", account.getId(), e);
                    OrderPosition position = OrderPosition.builder()
                            .order(savedOrder)
                            .account(account)
                            .status("ERROR")
                            .errorMessage("Error: " + e.getMessage())
                            .build();
                    orderPositionRepository.save(position);
                }
                //create a single AlertTradingEntity record with messageTradingViewDTO
                AlertTradingEntity alertTradingEntity = AlertTradingEntity.builder()
                        .action(AcctionTrading.fromString(messageTradingViewDTO.getAction()))
                        .instrument(messageTradingViewDTO.getInstrument())
                        .timestamp(Convert.convertStringToDateTime(messageTradingViewDTO.getTimestamp()))
                        .signalToken(messageTradingViewDTO.getSignalToken())
                        .maxLag(messageTradingViewDTO.getMaxLag())
                        .investmentType(messageTradingViewDTO.getInvestmentType())
                        .amount(Double.valueOf(messageTradingViewDTO.getAmount()))
                        .status("pending")
                        .build();
                AlertTradingEntity saveAlertTradingEntity = alertTradingRepository.save(alertTradingEntity);
                //sync to supabase
                //alertTradingService.saveAndSyncAlert(saveAlertTradingEntity);
            });
        }
    }

    // Wait for all account processing to complete (optional)
    // CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    @Transactional
    public void processWebhookClose(MessageTradingViewDTO webhookDTO) {

        log.info("Processing close order for signalToken: {}", webhookDTO.getSignalToken());
        //Lấy ra Botentity có signalToken = webhookDTO.getSignalToken()
        BotsEntity bot = botsRepository.findBySignalToken(webhookDTO.getSignalToken())
                .orElseThrow(() -> new RuntimeException("Bot not found with signal token: " + webhookDTO.getSignalToken()));

        Symbol symbol = Symbol.fromString6(webhookDTO.getInstrument());
        TradeSide tradeSideInput = TradeSide.fromString(AcctionTrading.fromString(webhookDTO.getAction()).getValue());
        List<OrderEntity> openOrders = orderRepository.findOpenOrdersBySymbolIdAndBotSignalTokenAndTradeSide(
                symbol.getId(), tradeSideInput, webhookDTO.getSignalToken());

        if (openOrders.isEmpty()) {
            log.warn("No open orders found for signalToken: {} and symbol: {}",
                    webhookDTO.getSignalToken(), symbol.getId());
            return;
        }
        // Danh sách để lưu trữ tất cả các CompletableFuture
        List<CompletableFuture<Void>> allFutures = new ArrayList<>();

        for (OrderEntity order : openOrders) {
            CompletableFuture<Void> positionFuture = CompletableFuture.runAsync(() -> {
                List<OrderPosition> positions = orderPositionRepository.findByOrderId(order.getId());

                // Xử lý song song các vị thế
                for (OrderPosition position : positions) {
                    if (!"OPEN".equals(position.getStatus())) {
                        continue;
                    }
                    AccountEntity account = position.getAccount();
                    String clientMsgId = generateClientMsgId() + "CLOSE_POSITION";
                    //save order
                    OrderPosition positionNew = OrderPosition.builder()
                            .clientMsgId(clientMsgId)
                            .order(order)
                            .orderType("CLOSE_POSITION")
                            .account(account)
                            .status("PENDING")
                            .build();
                    OrderPosition savedPosition = orderPositionRepository.saveAndFlush(positionNew);
                    // Tạo một CompletableFuture cho mỗi vị thế
                    try {
                        CTraderConnection connection = connectionService.getConnection(account.getId());
                        if (connection == null) {
                            log.error("No active connection for account: {}", account.getId());
                            return;
                        }
                        // Đóng vị thế
                        CompletableFuture<String> future = cTraderApiService.closePosition(connection, clientMsgId,
                                 position.getPositionId(), position.getVolumeSent());
//                        future.thenAccept(result -> {
//                            ResponseCtraderDTO responseCtraderDTO = validateRepsone.formatResponsePlaceOrder(result);
//                            if (responseCtraderDTO.getPayloadReponse() == PayloadType.PROTO_OA_ORDER_ERROR_EVENT.getValue()) {
//                                //save position
//                                position.setErrorCode(responseCtraderDTO.getErrorCode());
//                                position.setErrorMessage(responseCtraderDTO.getDescription());
//                                position.setStatus(ProtoOAExecutionType.ORDER_REJECTED.getStatus());
//                                position.setPayloadType(PayloadType.PROTO_OA_ORDER_ERROR_EVENT.name());
//                                position.setClientMsgId(responseCtraderDTO.getClientMsgId());
//                                orderPositionRepository.save(position);
//                                return;
//                            }
//                            position.setStatus("CLOSED");
//                            orderPositionRepository.save(position);

                        // Check if all positions for this order are closed
//                            List<OrderPosition> openPositions = orderPositionRepository.findByOrderId(order.getId()).stream()
//                                    .filter(p -> "OPEN".equals(p.getStatus()))
//                                    .toList();
//
//                            if (openPositions.isEmpty()) {
//                                order.setStatus("CLOSED");
//                                order.setCloseTime(LocalDateTime.now());
//                                orderRepository.save(order);
//                            }

//                            log.info("Position closed successfully for account: {}, positionId: {}",
//                                    account.getId(), position.getPositionId());
//                        }).exceptionally(ex -> {
//                            position.setStatus("ERROR_CLOSING");
//                            position.setErrorMessage("Error closing: " + ex.getMessage());
//                            orderPositionRepository.save(position);
//                            log.error("Failed to close position for account: {}", account.getId(), ex);
//                            return null;
//                        });
                    } catch (Exception e) {
                        log.error("Error closing position for account: {}", account.getId(), e);
                        position.setStatus("ERROR_CLOSING");
                        position.setErrorMessage("Error closing: " + e.getMessage());
                        orderPositionRepository.save(position);
                    }
                }
            });
        }

        // Tùy chọn: đợi tất cả các vị thế được xử lý xong
        // CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
    }

    public List<OrderEntity> getOrdersByAccountId(UUID accountId) {
        return orderRepository.findByAccountId(accountId);
    }

    public List<OrderEntity> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<OrderPosition> getPositionsByOrderId(UUID orderId) {
        return orderPositionRepository.findByOrderId(orderId);
    }

    private String generateClientMsgId() {
        // Generate a unique client message ID for tracking responses
        return prefix + UUID.randomUUID().toString().substring(0, 6) + "_" + System.nanoTime();
    }

    public List<OrderPosition> getPositionsByAccountId(UUID accountId) {
        return orderPositionRepository.findByAccountId(accountId);
    }
}