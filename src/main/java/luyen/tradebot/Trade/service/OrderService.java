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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
                                .volumeMultiplier(account.getVolumeMultiplier())
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
                            .volumeMultiplier(account.getVolumeMultiplier())
                            .originalVolume(webhookDTO.getVolume())
                            .orderType("NEW_ORDER")
                            .status("PENDING")
                            .tradeSide(TradeSide.fromValue(webhookDTO.getTradeSide()).toString())
                            .symbol(Symbol.fromId(webhookDTO.getSymbol()).toString())
                            .ctidTraderAccountId(account.getCtidTraderAccountId().toString())
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
                            .payloadType(PayloadType.PROTO_OA_NEW_ORDER_REQ)
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
                alertTradingService.saveAndSyncAlert(saveAlertTradingEntity);
            });
        }
    }

    // Wait for all account processing to complete (optional)
    // CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    @Transactional
    public void processWebhookClose(MessageTradingViewDTO webhookDTO) {
        log.info("Processing close order for signalToken: {}", webhookDTO.getSignalToken());
        
        Symbol symbol = Symbol.fromString6(webhookDTO.getInstrument());
        TradeSide tradeSideInput = TradeSide.fromString(AcctionTrading.fromString(webhookDTO.getAction()).getValue());
        OrderEntity openOrder = orderRepository.findOpenOrdersBySymbolIdAndBotSignalTokenAndTradeSide(
                symbol.getId(), tradeSideInput, webhookDTO.getSignalToken())
                .orElseThrow(() -> new RuntimeException("Order not found with signal token: " + webhookDTO.getSignalToken()));
        
        if (openOrder == null) {
            log.warn("No open orders found for signalToken: {} and symbol: {}",
                    webhookDTO.getSignalToken(), symbol.getId());
            return;
        }
        
        // Get all positions that need to be closed in a single query
        List<OrderPosition> positions = orderPositionRepository.findByOrderIdAndStatusAndOrderType(
                openOrder.getId(), "OPEN", OrderTypeSystem.NEW_ORDERS.getName());
        
        if (positions.isEmpty()) {
            log.info("No open positions found for order: {}", openOrder.getId());
            return;
        }
        
        log.info("Found {} positions to close for order: {}", positions.size(), openOrder.getId());
        
        // Group positions by connection to minimize connection lookups
        Map<UUID, List<OrderPosition>> positionsByAccountId = positions.stream()
                .filter(position -> "OPEN".equals(position.getStatus()))
                .collect(Collectors.groupingBy(position -> position.getAccount().getId()));
        
        // Create a single AlertTradingEntity record for this webhook
        AlertTradingEntity alertTradingEntity = AlertTradingEntity.builder()
                .action(AcctionTrading.fromString(webhookDTO.getAction()))
                .instrument(webhookDTO.getInstrument())
                .timestamp(Convert.convertStringToDateTime(webhookDTO.getTimestamp()))
                .signalToken(webhookDTO.getSignalToken())
                .maxLag(webhookDTO.getMaxLag())
                .investmentType(webhookDTO.getInvestmentType())
                .amount(Double.valueOf(webhookDTO.getAmount()))
                .status("pending")
                .build();
        AlertTradingEntity savedAlertTradingEntity = alertTradingRepository.save(alertTradingEntity);
        
        // Process each account's positions in parallel
        List<CompletableFuture<Void>> accountFutures = new ArrayList<>();
        
        positionsByAccountId.forEach((accountId, accountPositions) -> {
            CompletableFuture<Void> accountFuture = CompletableFuture.runAsync(() -> {
                // Get connection once per account
                CTraderConnection connection = connectionService.getConnection(accountId);
                if (connection == null) {
                    log.error("No active connection for account: {}", accountId);
                    accountPositions.forEach(position -> {
                        position.setStatus("ERROR_CLOSING");
                        position.setErrorMessage("No active connection for account");
                        orderPositionRepository.save(position);
                    });
                    return;
                }
                
                // Create all new position records in batch
                List<OrderPosition> newPositions = new ArrayList<>();
                Map<String, OrderPosition> clientMsgIdToPosition = new HashMap<>();
                
                for (OrderPosition position : accountPositions) {
                    String clientMsgId = position.getClientMsgId() + "_" + "CLOSE_POSITION";
                    OrderPosition positionNew = OrderPosition.builder()
                            .clientMsgId(clientMsgId)
                            .order(openOrder)
                            .orderType("CLOSE_POSITION")
                            .account(position.getAccount())
                            .status("PENDING")
                            .tradeSide(openOrder.getTradeSide().toString())
                            .symbol(openOrder.getSymbol().toString())
                            .ctidTraderAccountId(position.getAccount().getCtidTraderAccountId().toString())
                            .build();
                    newPositions.add(positionNew);
                    clientMsgIdToPosition.put(clientMsgId, position);
                }
                
                // Save all new positions in a single transaction
                List<OrderPosition> savedPositions = orderPositionRepository.saveAllAndFlush(newPositions);
                
                // Send all close commands in parallel
                List<CompletableFuture<String>> closeFutures = new ArrayList<>();
                
                for (OrderPosition newPosition : savedPositions) {
                    OrderPosition originalPosition = clientMsgIdToPosition.get(newPosition.getClientMsgId());
                    try {
                        // Send close position command
                        CompletableFuture<String> closeFuture = cTraderApiService.closePosition(
                                connection, 
                                newPosition.getClientMsgId(),
                                originalPosition.getPositionId(), 
                                originalPosition.getVolumeSent(), 
                                PayloadType.PROTO_OA_CLOSE_POSITION_REQ);
                        
                        // Handle response
                        closeFuture.whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Error closing position: {}", newPosition.getClientMsgId(), ex);
                                newPosition.setStatus("ERROR_CLOSING");
                                newPosition.setErrorMessage("Error closing: " + ex.getMessage());
                                orderPositionRepository.save(newPosition);
                            } else {
                                log.info("Successfully sent close command for position: {}", newPosition.getClientMsgId());
                            }
                        });
                        
                        closeFutures.add(closeFuture);
                    } catch (Exception e) {
                        log.error("Error sending close command for position: {}", newPosition.getClientMsgId(), e);
                        newPosition.setStatus("ERROR_CLOSING");
                        newPosition.setErrorMessage("Error closing: " + e.getMessage());
                        orderPositionRepository.save(newPosition);
                    }
                }
                
                // Wait for all close commands to be sent (optional)
                // CompletableFuture.allOf(closeFutures.toArray(new CompletableFuture[0])).join();
            });
            
            accountFutures.add(accountFuture);
        });
        
        // Sync alert to supabase (do this outside the parallel processing to avoid contention)
        alertTradingService.saveAndSyncAlert(savedAlertTradingEntity);
        
        // Optionally wait for all accounts to be processed
        // CompletableFuture.allOf(accountFutures.toArray(new CompletableFuture[0])).join();
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