package luyen.tradebot.Trade.service;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.util.DateUtil;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.ConnectStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private final Map<Long, CTraderConnection> connections = new ConcurrentHashMap<>();

    // Scheduled executor để thực hiện kiểm tra kết nối định kỳ
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        log.info("Initializing cTrader connections...");
        List<AccountEntity> activeAccounts = accountRepository.findByIsActive(true);
        for (AccountEntity account : activeAccounts) {
            connectAccount(account);
        }
    }

    public void connectAccount(AccountEntity account) {
        try {
            // Create a new connection to cTrader API
            CTraderConnection connection = cTraderApiService.connect(
                    account.getId(),
                    account.getClientId(),
                    account.getClientSecret(),
                    account.getAccessToken(),
                    account.getTypeAccount(),
                    this
            );

            // Store the connection
            connections.put(account.getId(), connection);

            // Update account status
            account.setConnected(true);
            account.setConnectionStatus(AccountStatus.CONNECT);
            account.setLastConnected(new Date());
            accountRepository.save(account);

            log.info("Successfully connected account: {} ({})",
                    account.getAccountId(), account.getTypeAccount());
            // If the account has a trader account ID already, authenticate it
            if (account.getCtidTraderAccountId() != 0) {
                try {
                    connection.authenticateTraderAccount(account.getCtidTraderAccountId())
                            .thenAccept(success -> {
//                                if (success) {
//                                    account.setAuthenticated(true);
//                                    account.setConnectionStatus(AccountStatus.AUTHENTICATED);
//                                    accountRepository.save(account);
//                                    log.info("Successfully authenticated trader account: {}",
//                                            account.getCtidTraderAccountId());
//                                }
                            });
                } catch (Exception e) {
                    log.error("Failed to authenticate trader account: {}",
                            account.getCtidTraderAccountId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to connect account: {} ({})",
                    account.getAccountId(), account.getTypeAccount(), e);

            account.setConnected(false);
            account.setConnectionStatus(AccountStatus.ERROR);
            account.setErrorMessage(e.getMessage());
            accountRepository.save(account);
        }
    }

    public void disconnectAccount(AccountEntity account) {
        CTraderConnection connection = connections.get(account.getId());
        if (connection != null) {
            try {
                connection.disconnect();
                connections.remove(account.getId());

                account.setConnected(false);
                account.setConnectionStatus(AccountStatus.DISCONNECT);
                accountRepository.save(account);

                log.info("Successfully disconnected account: {}", account.getAccountId());
            } catch (Exception e) {
                log.error("Failed to disconnect account: {}", account.getAccountId(), e);
            }
        }
    }

    public void reconnect(CTraderConnection connection) {
        Long accountId = connection.getAccountId();
        log.info("Attempting to reconnect for account: " + accountId);
        connection.close(); // Đóng kết nối cũ nếu còn mở
        connections.remove(accountId);
        // Tạo kết nối mới
        CTraderConnection newConnection = new CTraderConnection(accountId, connection.getClientId(), connection.getSecretId(),
                connection.getAccessToken(),this, connection.getWsUrl());
        connections.put(accountId, newConnection);
        newConnection.connect();
    }
    public void sendMessageToAccount(String accountId, String message) {
        CTraderConnection connection = connections.get(accountId);
        if (connection != null && connection.isConnected()) {
            connection.sendMessage(message);
        } else {
            log.warn("No active connection for account: " + accountId);
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
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkAndReconnectAccounts() {
        log.debug("Checking for disconnected accounts...");
        List<AccountEntity> disconnectedAccounts = accountRepository.findActiveDisconnectedAccounts();

        for (AccountEntity account : disconnectedAccounts) {
            log.info("Reconnecting account: {}", account.getAccountId());
            connectAccount(account);
        }
    }

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
                    connectAccount(account);
                }

                log.info("Successfully refreshed token for account: {}", account.getAccountId());
            } catch (Exception e) {
                log.error("Failed to refresh token for account: {}", account.getAccountId(), e);
            }
        }
    }


    // Get an active connection for an account
    public CTraderConnection getConnection(Long accountId) {
        return connections.get(accountId);
    }

    // New method to fetch available accounts using access token
    public List<Map<String, Object>> getAvailableAccounts(String accessToken) {
        return cTraderApiService.getAccounts(accessToken);
    }

}