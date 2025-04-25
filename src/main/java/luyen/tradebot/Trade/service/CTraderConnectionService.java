package luyen.tradebot.Trade.service;


import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.repository.ConnectedRepository;
import luyen.tradebot.Trade.repository.OrderPositionRepository;
import luyen.tradebot.Trade.util.enumTraderBot.ConnectStatus;
import luyen.tradebot.Trade.util.enumTraderBot.ErrorCode;
import luyen.tradebot.Trade.util.enumTraderBot.ProtoOAExecutionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CTraderConnectionService {

    private final AccountRepository accountRepository;
    private final CTraderApiService cTraderApiService;

    private final ConnectedRepository connectedRepository;

    private final OrderPositionRepository orderPositionRepository;



    // Lưu trữ thông tin kết nối với khóa là accountId (không phải clientId)
    private final Map<UUID, CTraderConnection> connections = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing cTrader connections...");
        List<AccountEntity> activeAccounts = accountRepository.findByIsActiveAndAuthenticated(true, true);
        for (AccountEntity account : activeAccounts) {
            connectAccount(account.getId());
        }
    }

    public UUID getAccountId(CTraderConnection connection) {
        return connection.getAccountId();
    }

    public void saveConnectionDetails(CTraderConnection connection) {
        try {
            UUID accountId = getAccountId(connection);
            ConnectedEntity connectedEntity = connectedRepository.findByAccountId(accountId)
                    .orElse(new ConnectedEntity());
            AccountEntity asyncAccount = accountRepository.findById(accountId).orElse(null);
            connectedEntity.setAccount(asyncAccount);
            connectedEntity.setConnectionStatus(ConnectStatus.CONNECTED);
            connectedEntity.setLastConnectionTime(new Date());
            connectedRepository.save(connectedEntity);
            log.info("Connection details saved for account: {}", accountId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveConnectionAuthenticated(CTraderConnection connection) {
        try {
            UUID accountId = getAccountId(connection);
            ConnectedEntity connectedEntity = connectedRepository.findByAccountId(accountId)
                    .orElse(new ConnectedEntity());
            AccountEntity asyncAccount = accountRepository.findById(accountId).orElse(null);
            connectedEntity.setAccount(asyncAccount);
            connectedEntity.setConnectionStatus(ConnectStatus.CONNECTED);
            connectedEntity.setAuthenticated(true);
            connectedEntity.setLastConnectionTime(new Date());
            connectedRepository.save(connectedEntity);
            log.info("Connection Authenticated: {}", accountId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void processOrderErrorEvent(JsonNode rootNode) {
        /*{
           "payloadType":2132,
           "clientMsgId":"trade365_40ce75b8",
           "payload":{
              "errorCode":"TRADING_BAD_VOLUME",
              "ctidTraderAccountId":42684029,
              "description":"Order volume = 0.00 is smaller than minimum allowed volume = 0.01."
           }
        }*/
        String clientMsgId = rootNode.path("clientMsgId").asText();
        String orderStatus = ProtoOAExecutionType.ORDER_REJECTED.getStatus();
        String errorCode = rootNode.path("payload").has("errorCode") ?
                rootNode.path("payload").get("errorCode").asText(null) : null;
        String descriptionError = rootNode.path("payload").has("description") ?
                rootNode.path("payload").get("description").asText(null) : null;


        orderPositionRepository.updateErrorCodeAndErrorMessageByClientMsgId(
                errorCode,
                descriptionError != null ? descriptionError : errorCode != null ? ErrorCode.fromName(errorCode).getDescription() : null,
                orderStatus,
                clientMsgId);
//        OrderRepository orderRepository = SpringContextHolder.getBean(OrderRepository.class);
//        orderRepository.updateStatusById(
//                ProtoOAExecutionType.ORDER_REJECTED.getStatus(), );


    }
    public void processOrderExecutionResponse(JsonNode rootNode) {
        int executionType = rootNode.path("payload").path("executionType").asInt();
        String clientMsgId = rootNode.path("clientMsgId").asText();
        int positionId = rootNode.path("payload").path("position").path("positionId").asInt();
        String orderStatus = ProtoOAExecutionType.fromCode(executionType).getStatus();
        String errorCode = rootNode.path("payload").has("errorCode") ?
                rootNode.path("payload").get("errorCode").asText(null) : null;
        //update order_postion theo orderId và positionId
        orderPositionRepository.updateByOrderCtraderIdAndPositionId(
                ProtoOAExecutionType.fromCode(executionType).getDescription(),
                errorCode != null ? ErrorCode.fromName(errorCode).getDescription() : null,
                errorCode, orderStatus, clientMsgId);
    }
    @Transactional()
    public void connectAccount(UUID accountId) {
        AccountEntity freshAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        try {
            // Create a new connection to cTrader API
            CTraderConnection connection = cTraderApiService.connect(
                    freshAccount.getId(),
                    freshAccount.getClientId(),
                    freshAccount.getClientSecret(),
                    freshAccount.getAccessToken(),
                    freshAccount.getTypeAccount(),
                    this,
                    null
            );
            // Store the connection
            connections.put(freshAccount.getId(), connection);
            // Update account status
            log.info("Successfully connected account: {} ({})",
                    freshAccount.getAccountId(), freshAccount.getTypeAccount());

            // If the account has a trader account ID already, authenticate it
            if (freshAccount.getCtidTraderAccountId() != 0) {
                try {
                    final int traderAccountId = freshAccount.getCtidTraderAccountId();
                    connection.authenticateTraderAccount(traderAccountId)
                            .thenAccept(success -> {
                                if (success != null) {
                                    log.info("Save authenticated trader account: {}", traderAccountId);
                                }
                            });
                } catch (Exception e) {
                    log.error("Failed to authenticate trader account: {}",
                            freshAccount.getCtidTraderAccountId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to connect account: ({})", freshAccount.getAccountId(), e);
            ConnectedEntity connectedEntity = connectedRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
            connectedEntity.setConnected(false);
            connectedEntity.setConnectionStatus(ConnectStatus.ERROR);
            connectedEntity.setErrorMessage(e.getMessage());
            connectedRepository.save(connectedEntity);
        }
    }

    public void disconnectAccount(AccountEntity account) {
        CTraderConnection connection = connections.get(account.getId());
        if (connection != null) {
            try {
                connection.disconnect();
                connections.remove(account.getId());

                log.info("Successfully disconnected account: {}", account.getAccountId());
            } catch (Exception e) {
                log.error("Failed to disconnect account: {}", account.getAccountId(), e);
            }
        }
    }

    public void reconnect(CTraderConnection connection) {
        UUID accountId = connection.getAccountId();
        log.info("Attempting to reconnect for account: " + accountId);
        connection.close(); // Đóng kết nối cũ nếu còn mở
        connections.remove(accountId);
        // Tạo kết nối mới
        CTraderConnection newConnection = cTraderApiService.connect(
                accountId,
                connection.getClientId(),
                connection.getSecretId(),
                connection.getAccessToken(),
                null,
                this,
                connection.getWsUrl()
        );
        newConnection.setAuthenticatedTraderAccountId(connection.getAuthenticatedTraderAccountId());
        connections.put(accountId, newConnection);
        // check khi connect thành công thì mới tiếp tục authenticate trader account
        if (newConnection.isConnectionSuccessful()) {
            newConnection.authenticateTraderAccount(newConnection.getAuthenticatedTraderAccountId());
            return;
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

}