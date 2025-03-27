package luyen.tradebot.Trade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.OrderWebhookDTO;
import luyen.tradebot.Trade.dto.request.OrderDTO;
import luyen.tradebot.Trade.dto.respone.OrderResponseCtrader;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.BotEntity;
import luyen.tradebot.Trade.model.OrderEntity;
import luyen.tradebot.Trade.model.OrderPosition;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.repository.BotRepository;
import luyen.tradebot.Trade.repository.OrderPositionRepository;
import luyen.tradebot.Trade.repository.OrderRepository;
import luyen.tradebot.Trade.util.ValidateRepsone;
import luyen.tradebot.Trade.util.enumTraderBot.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final BotRepository botRepository;
    private final OrderPositionRepository orderPositionRepository;
    private final CTraderConnectionService connectionService;

    private final CTraderApiService cTraderApiService;

    public OrderEntity placeOrder(OrderDTO orderDTO) {
        BotEntity bot = (BotEntity) botRepository.findByBotName(orderDTO.getBotName())
                .orElseThrow(() -> new RuntimeException("Account not found"));
//        if (!account.isConnected()) {
//            throw new RuntimeException("Account is not connected");
//        }

//        OrderEntity order = OrderEntity.builder()
//                .symbol(orderDTO.getSymbol())
//                .symbolId(orderDTO.getSymbol() != null ? orderDTO.getSymbol().getId() : orderDTO.getSymbol().getId())
//                .tradeSide(orderDTO.getTradeSide())
//                .volume(orderDTO.getVolume())
//                .status("PENDING")
//                .orderType(orderDTO.getOrderType())
//                .openTime(LocalDateTime.now())
//                .account(account)
//                .build();

//        OrderEntity savedOrder = orderRepository.save(order);

//        CTraderConnection connection = connectionService.getConnection(account.getId());
//        if (connection == null) {
//            savedOrder.setStatus("ERROR");
//            savedOrder.setComment("No active connection for account");
//            return orderRepository.save(savedOrder);
//        }
//
//        OrderPosition position = OrderPosition.builder()
//                .order(savedOrder)
//                .account(account)
//                .status("PENDING")
//                .build();
//
//        OrderPosition savedPosition = orderPositionRepository.save(position);
//
//        CompletableFuture<String> future = connection.placeOrder(
//                order.getSymbol(),
//                order.getTradeSide(),
//                order.getVolume(),
//                order.getOrderType()
//        );

//        future.thenAccept(positionId -> {
//            savedPosition.setPositionId(positionId);
//            savedPosition.setStatus("OPEN");
//            orderPositionRepository.save(savedPosition);
//
//            savedOrder.setStatus("OPEN");
//            orderRepository.save(savedOrder);
//        }).exceptionally(ex -> {
//            savedPosition.setStatus("ERROR");
//            savedPosition.setErrorMessage(ex.getMessage());
//            orderPositionRepository.save(savedPosition);
//
//            savedOrder.setStatus("ERROR");
//            savedOrder.setComment("Error: " + ex.getMessage());
//            orderRepository.save(savedOrder);
//            return null;
//        });

//        return savedOrder;
        return null;
    }

    public OrderEntity closeOrder(Long orderId, Long accountId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        OrderPosition position = orderPositionRepository.findByOrderIdAndAccountId(orderId, accountId)
                .orElseThrow(() -> new RuntimeException("Position not found for this order and account"));

        if (!"OPEN".equals(position.getStatus())) {
            throw new RuntimeException("Position is not open");
        }

        if (!account.isConnected()) {
            throw new RuntimeException("Account is not connected");
        }

        CTraderConnection connection = connectionService.getConnection(account.getId());
        if (connection == null) {
            position.setErrorMessage("No active connection for account");
            orderPositionRepository.save(position);
            return order;
        }

        CompletableFuture<String> future = connection.closePosition(position.getPositionId());

        future.thenAccept(result -> {
            position.setStatus("CLOSED");
            orderPositionRepository.save(position);

            // Check if all positions are closed
            List<OrderPosition> openPositions = orderPositionRepository.findByOrderId(orderId).stream()
                    .filter(p -> "OPEN".equals(p.getStatus()))
                    .toList();

            if (openPositions.isEmpty()) {
                order.setStatus("CLOSED");
                order.setCloseTime(LocalDateTime.now());
                orderRepository.save(order);
            }
        }).exceptionally(ex -> {
            position.setStatus("ERROR_CLOSING");
            position.setErrorMessage("Error closing: " + ex.getMessage());
            orderPositionRepository.save(position);
            return null;
        });

        return order;
    }

    public void processWebhookOrder(OrderWebhookDTO webhookDTO) {
        BotEntity bot = botRepository.findBySignalToken(webhookDTO.getSignalToken())
                .orElseThrow(() -> new RuntimeException("Bot not found with signal token: " + webhookDTO.getSignalToken()));

        List<AccountEntity> accounts = accountRepository.findByBotIdAndIsActiveAndIsAuthenticated(
                bot.getId(), true, true);

        if (accounts.isEmpty()) {
            log.warn("No active authenticated accounts found for bot: {}", bot.getId());
            return;
        }

        Symbol symbol = Symbol.valueOf(webhookDTO.getSymbol());
        TradeSide tradeSide = TradeSide.valueOf(webhookDTO.getTradeSide());
        OrderType orderType = OrderType.valueOf(webhookDTO.getOrderType());

        // Create a single order record
        OrderEntity order = OrderEntity.builder()
                .symbol(Symbol.fromString(webhookDTO.getSymbol()))
                .symbolId(Symbol.valueOf(webhookDTO.getSymbol()).getId())
                .tradeSide(TradeSide.fromString(webhookDTO.getTradeSide()))
                .volume(webhookDTO.getVolume())
                .status("OPEN")
                .orderType(OrderType.fromString(webhookDTO.getOrderType()))
                .comment("Created via webhook for bot: " + bot.getBotName())
                .openTime(LocalDateTime.now())
                .account(accounts.get(0)) // Use the first account as the reference account
                .botId(bot.getId()) // Use the first account as the reference account
                .build();

        OrderEntity savedOrder = orderRepository.save(order);

        // Process order for each account
        for (AccountEntity account : accounts) {
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
                    continue;
                }

                OrderPosition position = OrderPosition.builder()
                        .order(savedOrder)
                        .account(account)
                        .status("PENDING")
                        .build();

                OrderPosition savedPosition = orderPositionRepository.save(position);

                CompletableFuture<String> future = cTraderApiService.placeOrder(connection, symbol, tradeSide, webhookDTO.getVolume(), orderType);
                future.thenAccept(result -> {
                    ResponseCtraderDTO responseCtraderDTO = ValidateRepsone.formatResponsePlaceOrder(result);
                    //kiểm tra payloadReponse có trong list của enumer không và seting giá trị vào positionEntity
                    PayloadType payloadType = PayloadType.fromValue(responseCtraderDTO.getPayloadReponse());
                    if (payloadType == PayloadType.UNKNOWN) {
                        log.warn("⚠ Received an unknown payloadType: " + payloadType);
                    } else {
                        savedPosition.setPayloadType(payloadType.toString());
                    }

                    //làm tương tự payloadType nhưng với executionType
                    ProtoOAExecutionType executionType = ProtoOAExecutionType.fromCode(responseCtraderDTO.getExecutionType());
                    if (executionType == ProtoOAExecutionType.UNKNOWN) {
                        log.warn("⚠ Received an unknown executionType: " + executionType);
                    } else {
                        savedPosition.setExecutionType(executionType.toString());
                    }

                    //kiểm tra nếu có lỗi thì được hiện setErrorMessage vào trong savePosition
                    if (!responseCtraderDTO.getErrorCode().equals("N/A")) {
                        savedPosition.setErrorMessage(responseCtraderDTO.getDescription());
                        savedPosition.setErrorCode(responseCtraderDTO.getErrorCode());
                        orderPositionRepository.save(savedPosition);
                        return;
                    }
                    savedPosition.setPositionId(responseCtraderDTO.getPositionId());
                    savedPosition.setStatus("OPEN");
                    orderPositionRepository.save(savedPosition);
                    log.info("Order placed successfully for account: {}, positionId: {}",
                            account.getId(), responseCtraderDTO.getPositionId());
                }).exceptionally(ex -> {
                    savedPosition.setStatus("ERROR");
                    savedPosition.setErrorMessage("Error: " + ex.getMessage());
                    orderPositionRepository.save(savedPosition);
                    log.error("Failed to place order for account: {}", account.getId(), ex);
                    return null;
                });
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
        }
    }

    public void processWebhookClose(OrderWebhookDTO webhookDTO) {
        Symbol symbol = Symbol.fromString(webhookDTO.getSymbol());
        List<OrderEntity> openOrders = orderRepository.findOpenOrdersBySymbolIdAndBotSignalToken(
                symbol.getId(), webhookDTO.getSignalToken());

        if (openOrders.isEmpty()) {
            log.warn("No open orders found for signalToken: {} and symbol: {}",
                    webhookDTO.getSignalToken(), symbol.getId());
            return;
        }

        for (OrderEntity order : openOrders) {
            List<OrderPosition> positions = orderPositionRepository.findByOrderId(order.getId());

            for (OrderPosition position : positions) {
                if (!"OPEN".equals(position.getStatus())) {
                    continue;
                }

                AccountEntity account = position.getAccount();

                if (!account.isActive() || !account.isConnected()) {
                    log.warn("Account {} is not active or connected, skipping close", account.getId());
                    continue;
                }

                try {
                    CTraderConnection connection = connectionService.getConnection(account.getId());
                    if (connection == null) {
                        log.error("No active connection for account: {}", account.getId());
                        continue;
                    }

                    position.setStatus("CLOSING");
                    orderPositionRepository.save(position);

                    CompletableFuture<String> future = connection.closePosition(position.getPositionId());

                    future.thenAccept(result -> {
                        position.setStatus("CLOSED");
                        orderPositionRepository.save(position);

                        // Check if all positions for this order are closed
                        List<OrderPosition> openPositions = orderPositionRepository.findByOrderId(order.getId()).stream()
                                .filter(p -> "OPEN".equals(p.getStatus()))
                                .toList();

                        if (openPositions.isEmpty()) {
                            order.setStatus("CLOSED");
                            order.setCloseTime(LocalDateTime.now());
                            orderRepository.save(order);
                        }

                        log.info("Position closed successfully for account: {}, positionId: {}",
                                account.getId(), position.getPositionId());
                    }).exceptionally(ex -> {
                        position.setStatus("ERROR_CLOSING");
                        position.setErrorMessage("Error closing: " + ex.getMessage());
                        orderPositionRepository.save(position);
                        log.error("Failed to close position for account: {}", account.getId(), ex);
                        return null;
                    });
                } catch (Exception e) {
                    log.error("Error closing position for account: {}", account.getId(), e);
                    position.setStatus("ERROR_CLOSING");
                    position.setErrorMessage("Error closing: " + e.getMessage());
                    orderPositionRepository.save(position);
                }
            }
        }
    }

    public List<OrderEntity> getOrdersByAccountId(Long accountId) {
        return orderRepository.findByAccountId(accountId);
    }

    public List<OrderEntity> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<OrderPosition> getPositionsByOrderId(Long orderId) {
        return orderPositionRepository.findByOrderId(orderId);
    }

    public List<OrderPosition> getPositionsByAccountId(Long accountId) {
        return orderPositionRepository.findByAccountId(accountId);
    }
}