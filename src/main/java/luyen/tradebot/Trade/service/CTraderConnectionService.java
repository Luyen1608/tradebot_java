package luyen.tradebot.Trade.service;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.OrderWebhookDTO;
import luyen.tradebot.Trade.dto.request.PlaceOrderRequest;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import luyen.tradebot.Trade.model.OrderPosition;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.repository.ConnectedRepository;
import luyen.tradebot.Trade.repository.OrderPositionRepository;
import luyen.tradebot.Trade.util.enumTraderBot.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CTraderConnectionService {

    private final AccountRepository accountRepository;
    private final CTraderApiService cTraderApiService;
    private final OrderPositionRepository orderPositionRepository;
    private final ConnectedRepository connectedRepository;

    // Lưu trữ thông tin kết nối với khóa là accountId (không phải clientId)
    private final Map<UUID, CTraderConnection> connections = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing cTrader connections...");
        List<AccountEntity> activeAccounts = accountRepository.findByIsActive(true);
        // Sử dụng CompletableFuture để quản lý các kết nối song song tốt hơn
        List<CompletableFuture<Void>> connectionFutures = activeAccounts.stream()
                .map(account -> CompletableFuture.runAsync(() -> {
                    try {
                        if (account.isActive()){
                            log.info("Parallel connecting account: {}", account.getId());
                            connectAccount(account.getId());
                        }
                    } catch (Exception e) {
                        log.error("Error connecting account {} in parallel: {}", account.getId(), e.getMessage(), e);
                    }
                }))
                .toList();

        // Đợi tất cả các kết nối hoàn thành
        CompletableFuture.allOf(connectionFutures.toArray(new CompletableFuture[0])).join();

        log.info("Completed parallel connection for {} active accounts", activeAccounts.size());

    }

    @Transactional
    public synchronized void saveConnectionDetails(CTraderConnection connection) {
        try {
            UUID accountId = connection.getAccountId();
            AccountEntity asyncAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

            asyncAccount.setIsConnected(true);
            asyncAccount.setErrorMessage("");
            accountRepository.saveAndFlush(asyncAccount);
            // Đảm bảo kết nối được lưu trong map
            if (!connections.containsKey(accountId)) {
                connections.put(accountId, connection);
                log.info("Added connection to map for account: {}", accountId);
            }
//            ConnectedEntity connectedEntity = connectedRepository.findByAccountId(accountId)
//                    .orElse(new ConnectedEntity());
//            connectedEntity.setAccount(asyncAccount);
//            connectedEntity.setConnectionStatus(ConnectStatus.CONNECTED);
//            connectedEntity.setLastConnectionTime(new Date());
//            connectedRepository.save(connectedEntity);
            log.info("Connection details saved for account: {}", accountId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public synchronized void saveConnectionAuthenticated(CTraderConnection connection) {
        try {
            String clientMsgId = connection.getClientMsgId();
            UUID accountId = connection.getAccountId();
            log.info("Saving authentication details for account: {}", accountId);
            AccountEntity asyncAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

            asyncAccount.setIsAuthenticated(true);
            asyncAccount.setErrorMessage("");
            accountRepository.saveAndFlush(asyncAccount);
            if (connections.containsKey(accountId)) {
//                CTraderConnection existingConnection = connections.get(accountId);
//                existingConnection.setAuthenticatedTraderAccountId(connection.getAuthenticatedTraderAccountId());
                log.info("Updated authenticated trader account ID for account: {}", accountId);
            } else {
                connections.put(accountId, connection);
                log.info("Added authenticated connection to map for account: {}", accountId);
            }
            log.info("Connection Authenticated: {}", accountId);
            log.info("{} - Resend order : {} - {}",connection.getAccountId(), clientMsgId, connection.getAuthenticatedTraderAccountId());
            if (!"".equals(clientMsgId) && !clientMsgId.isEmpty()){
                // order placer
                try {
                Optional<OrderPosition> orderPositionOpt = orderPositionRepository.findByClientMsgIdLimitOne(clientMsgId);
                if (orderPositionOpt.isPresent()) {
                    OrderPosition orderPosition = orderPositionOpt.get();
//                OrderPosition orderPosition = orderPositionRepository.findByClientMsgIdLimitOne(clientMsgId)
//                        .orElseThrow(() -> new RuntimeException("OrderPosition not found with clientMsgId: " + clientMsgId));;
                int symbol = Symbol.fromString6(orderPosition.getSymbol()).getId();
                int tradeSide = TradeSide.fromString(orderPosition.getTradeSide()).getValue();
                int originVolumn = orderPosition.getOriginalVolume();
                int volumeMultiple = orderPosition.getOriginalVolume();
                int stopLoss = orderPosition.getStopLoss();
                int takePro = orderPosition.getTakeProfit();
                int relativeStop = orderPosition.getRelativeStopLoss();
                int realativeTake = orderPosition.getRelativeTakeProfit();
//                String clientMsgId = generateClientMsgId();
                PlaceOrderRequest request = PlaceOrderRequest.builder()
                        .connection(connection)
                        .clientMsgId(generateClientMsgId())
                        .symbol(symbol)
                        .tradeSide(tradeSide)
                        .volume(originVolumn)
                        .stopLoss(stopLoss)
                        .takeProfit(takePro)
                        .relativeStopLoss(relativeStop)
                        .relativeTakeProfit(realativeTake)
                        .orderType(OrderType.fromString("MARKET").getValue())
                        .account(orderPosition.getAccount())
//                        .savedOrder(savedOrder)
                        .payloadType(PayloadType.PROTO_OA_NEW_ORDER_REQ)
                        .build();

                log.info("{} - placeOrder : {} - {} - {}",connection.getAccountId(), clientMsgId, connection.getAuthenticatedTraderAccountId(), request);
                cTraderApiService.placeOrder(request);
                connection.setClientMsgId("");

            } else {
                    log.warn("{} - OrderPosition not found with clientMsgId: {} - Cannot retry failed order",
                                                            connection.getAccountId(), clientMsgId);
                                            // Reset clientMsgId nếu không tìm thấy order
                                                    connection.setClientMsgId("");
                }
            } catch (Exception e) {
                log.error("{} - Error while retrying failed order with clientMsgId: {} - {}",
                        connection.getAccountId(), clientMsgId, e.getMessage(), e);
                // Reset clientMsgId nếu có lỗi
                connection.setClientMsgId("");
            }

        }} catch (Exception e) {
            log.error("{} - Error while retrying failed order with clientMsgId: {} ",
                                                connection.getAccountId(), e.getMessage(), e);
                                // Reset clientMsgId nếu có lỗi
                                        connection.setClientMsgId("");
            throw new RuntimeException(e);
        }
    }

    @Transactional()
    public synchronized void connectAccount(UUID accountId) {
        if (connections.containsKey(accountId)) {
            log.info("Connection already exists for account: {}", accountId);
            return;
        }
        AccountEntity freshAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        try {
            log.info("Creating new connection for account: {} ({})",
                    freshAccount.getId(), freshAccount.getTypeAccount());
            // Create a new connection to cTrader API
            CTraderConnection connection = cTraderApiService.connect(
                    freshAccount.getId(),
                    freshAccount.getClientId(),
                    freshAccount.getClientSecret(),
                    freshAccount.getAccessToken(),
                    freshAccount.getTypeAccount(),
                    this,
                    null,
                    freshAccount.getVolumeMultiplier(),
                    freshAccount.getCtidTraderAccountId(),
                    ""
            );
//            if (!connection.isConnectionSuccessful()) {
//                log.warn("Connection not successful for account: {} ({})",
//                        freshAccount.getId(), freshAccount.getTypeAccount());
//                return;
//            }
            // Store the connection
            connections.put(freshAccount.getId(), connection);
            // Update account status
            log.info("Successfully connected account: {} ({})",
                    freshAccount.getAccountId(), freshAccount.getTypeAccount());
            // If the account has a trader account ID already, authenticate it
//            if (freshAccount.getCtidTraderAccountId() != 0) {
//                try {
//                    final int traderAccountId = freshAccount.getCtidTraderAccountId();
//                    log.info("Authenticating trader account: {} for account: {}",
//                            traderAccountId, freshAccount.getId());
//                    connection.authenticateTraderAccount(traderAccountId)
//                            .thenAccept(success -> {
//                                if (success != null) {
//                                    log.info("Successfully authenticated trader account: {} for account: {}",
//                                            traderAccountId, freshAccount.getId());
//
//                                }
//                            });
//                } catch (Exception e) {
//                    log.error("Failed to authenticate trader account: {} for account: {}",
//                            freshAccount.getCtidTraderAccountId(), freshAccount.getId(), e);
//                    connections.remove(freshAccount.getId());
//                    disconnectAccount(freshAccount.getId());
//                }
//            } else // remove connenct
//            {
//                log.info("Remove connection for account: {}", freshAccount.getAccountId());
//                connections.remove(freshAccount.getId());
//                disconnectAccount(freshAccount.getId());
//            }
        } catch (Exception e) {
            log.error("Failed to connect account: ({})", freshAccount.getAccountId(), e);
            freshAccount.setIsConnected(false);
            freshAccount.setIsAuthenticated(false);
            accountRepository.saveAndFlush(freshAccount);
//            ConnectedEntity connectedEntity = connectedRepository.findByAccountId(accountId)
//                    .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
//            connectedEntity.setConnected(false);
//            connectedEntity.setConnectionStatus(ConnectStatus.ERROR);
//            connectedEntity.setErrorMessage(e.getMessage());
//            connectedRepository.save(connectedEntity);
        }
    }

    public void disconnectAccount(UUID accountID) {
        try {
            CTraderConnection connection = connections.get(accountID);
            if (connection != null) {
                connection.disconnect();
                connections.remove(accountID);
                log.info("Successfully disconnected account: {}", accountID);
            }
        } catch (Exception e) {
            log.error("Failed to disconnect account: {}", accountID, e);
        }
    }

    @Transactional
    public synchronized void reconnect(CTraderConnection connection, String clientMsgId) {
        UUID accountId = connection.getAccountId();
        log.info("{} - Attempting to reconnect for account: {} - {} - {}",accountId,connection.getAuthenticatedTraderAccountId(), clientMsgId, accountId);
        String oldClientId = connection.getClientId();
        String oldSecretId = connection.getSecretId();
        String oldAccessToken = connection.getAccessToken();
        String oldWsUrl = connection.getWsUrl();
        Double oldVolumeMultiplier = connection.getVolumeMultiplier();
        int oldAuthenticatedTraderAccountId = connection.getAuthenticatedTraderAccountId();
        connection.close(); // Đóng kết nối cũ nếu còn mở
        connections.remove(accountId);
        // Tạo kết nối mới
        try {
            // Tạo kết nối mới
            CTraderConnection newConnection = cTraderApiService.connect(
                    accountId,
                    oldClientId,
                    oldSecretId,
                    oldAccessToken,
                    null,
                    this,
                    oldWsUrl,
                    oldVolumeMultiplier,
                    oldAuthenticatedTraderAccountId,
                    clientMsgId
            );
            // Sao chép thông tin xác thực từ kết nối cũ
//            newConnection.setAuthenticatedTraderAccountId(connection.getAuthenticatedTraderAccountId());
            // Lưu kết nối mới vào map
            connections.put(accountId, newConnection);
            log.info("Created new connection for account: {}", accountId);
            // Chỉ xác thực tài khoản trader nếu kết nối thành công và có ID tài khoản trader
//            if (newConnection.isConnectionSuccessful() && newConnection.getAuthenticatedTraderAccountId() != 0) {
//                log.info("Authenticating trader account: {} for reconnected account: {}",
//                        newConnection.getAuthenticatedTraderAccountId(), accountId);
//                newConnection.authenticateTraderAccount(newConnection.getAuthenticatedTraderAccountId())
//                        .thenAccept(success -> {
//                            if (success != null) {
//                                log.info("Successfully authenticated trader account after reconnection: {}", newConnection.getAuthenticatedTraderAccountId());
//                            }
//                        });
//            } else {
//                log.warn("Reconnection successful but not authenticating trader account for account: {}", accountId);
//            }
        } catch (Exception e) {
            log.error("Failed to reconnect account: {}", accountId, e);
        }
    }

    // Thêm phương thức lấy danh sách kết nối
    public List<ConnectedEntity> getCurrentConnections() {
        return connections.entrySet().stream()
                .map(entry -> new ConnectedEntity(
                        entry.getKey(), // accountId
                        entry.getValue().isConnected() ? ConnectStatus.CONNECTED : ConnectStatus.DISCONNECTED // status
                ))
                .collect(Collectors.toList());
    }

    public void stopAllConnections() {
        for (CTraderConnection connection : connections.values()) {
            connection.close();
        }
        connections.clear();
    }


    // Scheduled task to check and reconnect accounts that are disconnected
//    @Scheduled(fixedRate = 60000) // Every minute
//    public void checkAndReconnectAccounts() {
//        log.debug("Checking for disconnected accounts...");
//        List<AccountEntity> disconnectedAccounts = accountRepository.findActiveDisconnectedAccounts();
//
//        for (AccountEntity account : disconnectedAccounts) {
//            log.info("Reconnecting account: {}", account.getAccountId());
//            connectAccount(account);
//        }
//    }

    // Scheduled task to refresh access tokens
//    @Scheduled(fixedRate = 3600000) // Every hour
    public void refreshTokens() {
//        log.debug("Refreshing access tokens...");
//        List<AccountEntity> activeAccounts = accountRepository.findByIsActive(true);
//
//        for (AccountEntity account : activeAccounts) {
//            try {
//                String newAccessToken = cTraderApiService.refreshToken(account.getRefreshToken());
//                account.setAccessToken(newAccessToken);
//                account.setTokenExpiry(DateUtil.plusDate(30));
//                accountRepository.save(account);
//
//                // Reconnect with new token if already connected
//                if (account.isConnected()) {
//                    disconnectAccount(account);
////                    connectAccount(account);
//                }
//
////                log.info("Successfully refreshed token for account: {}", account.getAccountId());
//            } catch (Exception e) {
//                log.error("Failed to refresh token for account: {}", account.getAccountId(), e);
//            }
//        }
    }


    // Get an active connection for an account
    public CTraderConnection getConnection(UUID accountId) {
        return connections.get(accountId);
    }

    // New method to fetch available accounts using access token
    public List<Map<String, Object>> getAvailableAccounts(String accessToken) {
        return cTraderApiService.getAccounts(accessToken);
    }
    private String generateClientMsgId() {
        // Generate a unique client message ID for tracking responses
        // Ví dụ: "myPrefix_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.nanoTime();
        return "trade365_" + UUID.randomUUID().toString().substring(0, 6) + "_" + System.nanoTime();
    }
}