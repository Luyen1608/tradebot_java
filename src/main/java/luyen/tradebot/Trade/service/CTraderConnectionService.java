package luyen.tradebot.Trade.service;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.util.DateUtil;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.ConnectStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CTraderConnectionService {

    private final AccountRepository accountRepository;
    private final CTraderApiService cTraderApiService;

    // Lưu trữ thông tin kết nối với khóa là accountId (không phải clientId)
    private final Map<UUID, CTraderConnection> connections = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing cTrader connections...");
        List<AccountEntity> activeAccounts = accountRepository.findByIsActiveAndAuthenticated(true, true);
        for (AccountEntity account : activeAccounts) {
            connectAccountInit(account.getId());
        }
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void connectAccount(UUID accountId) {
        AccountEntity freshAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        // Always get a fresh entity from the database
//        AccountEntity freshAccount = accountRepository.findById(accountId)
//                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
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
            freshAccount.setConnected(true);
            freshAccount.setConnectionStatus(AccountStatus.CONNECT);
            freshAccount.setLastConnected(new Date());
            accountRepository.save(freshAccount);

            log.info("Successfully connected account: {} ({})",
                    freshAccount.getAccountId(), freshAccount.getTypeAccount());

            // If the account has a trader account ID already, authenticate it
            if (freshAccount.getCtidTraderAccountId() != 0) {
                try {
                    final UUID finalAccountId = freshAccount.getId();
                    final int traderAccountId = freshAccount.getCtidTraderAccountId();

                    connection.authenticateTraderAccount(traderAccountId)
                            .thenAccept(success -> {
                                if (success != null) {
                                    // Get a fresh entity in this async context
                                    AccountEntity asyncAccount = accountRepository.findById(finalAccountId)
                                            .orElse(null);

                                    if (asyncAccount != null) {
                                        asyncAccount.setAuthenticated(true);
                                        asyncAccount.setConnectionStatus(AccountStatus.AUTHENTICATED);
                                        accountRepository.save(asyncAccount);
                                        log.info("Save authenticated trader account: {}", traderAccountId);
                                    }
                                }
                            });
                } catch (Exception e) {
                    log.error("Failed to authenticate trader account: {}",
                            freshAccount.getCtidTraderAccountId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to connect account: {} ({})",
                    freshAccount.getAccountId(), freshAccount.getTypeAccount(), e);

            freshAccount.setConnected(false);
            freshAccount.setConnectionStatus(AccountStatus.ERROR);
            freshAccount.setErrorMessage(e.getMessage());
            accountRepository.save(freshAccount);
        }
    }


    public void connectAccountInit(UUID accountId) {
        AccountEntity freshAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        // Always get a fresh entity from the database
//        AccountEntity freshAccount = accountRepository.findById(accountId)
//                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
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
            freshAccount.setConnected(true);
            freshAccount.setConnectionStatus(AccountStatus.CONNECT);
            freshAccount.setLastConnected(new Date());
            accountRepository.save(freshAccount);

            log.info("Successfully connected account: {} ({})",
                    freshAccount.getAccountId(), freshAccount.getTypeAccount());

            // If the account has a trader account ID already, authenticate it
            if (freshAccount.getCtidTraderAccountId() != 0) {
                try {
                    final UUID finalAccountId = freshAccount.getId();
                    final int traderAccountId = freshAccount.getCtidTraderAccountId();

                    connection.authenticateTraderAccount(traderAccountId)
                            .thenAccept(success -> {
                                if (success != null) {
                                    // Get a fresh entity in this async context
                                    AccountEntity asyncAccount = accountRepository.findById(finalAccountId)
                                            .orElse(null);

                                    if (asyncAccount != null) {
                                        asyncAccount.setAuthenticated(true);
                                        asyncAccount.setConnectionStatus(AccountStatus.AUTHENTICATED);
                                        accountRepository.save(asyncAccount);
                                        log.info("Save authenticated trader account: {}", traderAccountId);
                                    }
                                }
                            });
                } catch (Exception e) {
                    log.error("Failed to authenticate trader account: {}",
                            freshAccount.getCtidTraderAccountId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to connect account: {} ({})",
                    freshAccount.getAccountId(), freshAccount.getTypeAccount(), e);

            freshAccount.setConnected(false);
            freshAccount.setConnectionStatus(AccountStatus.ERROR);
            freshAccount.setErrorMessage(e.getMessage());
            accountRepository.save(freshAccount);
        }
    }

    // Keep the old method for backward compatibility

    public void disconnectAccount(AccountEntity account) {
        CTraderConnection connection = connections.get(account.getId());
        if (connection != null) {
            try {
                connection.disconnect();
                connections.remove(account.getId());

//                account.setConnected(false);
//                account.setConnectionStatus(AccountStatus.DISCONNECT);
//                accountRepository.save(account);

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

    public boolean isAccountConnected(String accountId) {
        CTraderConnection connection = connections.get(accountId);
        return connection != null && connection.isConnected();
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
        log.debug("Refreshing access tokens...");
        List<AccountEntity> activeAccounts = accountRepository.findByIsActive(true);

        for (AccountEntity account : activeAccounts) {
            try {
                String newAccessToken = cTraderApiService.refreshToken(account.getRefreshToken());
                account.setAccessToken(newAccessToken);
                account.setTokenExpiry(DateUtil.plusDate(30));
                accountRepository.save(account);

                // Reconnect with new token if already connected
                if (account.isConnected()) {
                    disconnectAccount(account);
//                    connectAccount(account);
                }

//                log.info("Successfully refreshed token for account: {}", account.getAccountId());
            } catch (Exception e) {
                log.error("Failed to refresh token for account: {}", account.getAccountId(), e);
            }
        }
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