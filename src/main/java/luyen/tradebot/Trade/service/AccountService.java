package luyen.tradebot.Trade.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.AccountRequestDTO;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.BotEntity;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.repository.BotRepository;
import luyen.tradebot.Trade.util.DateUtil;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final BotRepository botRepository;
    private final CTraderApiService cTraderApiService;
    private final CTraderConnectionService connectionService;


    public String getAccessToken(String clientId, String clientSecret) {
        return cTraderApiService.getAccessToken(clientId, clientSecret);
    }

    public List<Map<String, Object>> getAccounts(String accessToken) {
        return cTraderApiService.getAccounts(accessToken);
    }
    public CompletableFuture<List<Map<String, Object>>> getTraderAccounts(Long accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.isConnected()) {
            throw new RuntimeException("Account is not connected");
        }

        CTraderConnection connection = connectionService.getConnection(accountId);
        if (connection == null) {
            throw new RuntimeException("No active connection for this account");
        }

        return cTraderApiService.getTraderAccounts(connection);
    }

    public CompletableFuture<Boolean> authenticateTraderAccount(Long accountId, int ctidTraderAccountId, String traderAccountName) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.isConnected()) {
            throw new RuntimeException("Account is not connected");
        }

        CTraderConnection connection = connectionService.getConnection(accountId);
        if (connection == null) {
            throw new RuntimeException("No active connection for this account");
        }

        CompletableFuture<Boolean> future = cTraderApiService.authenticateTraderAccount(
                connection, ctidTraderAccountId);

        future.thenAccept(success -> {
            if (success) {
                account.setCtidTraderAccountId(ctidTraderAccountId);
                account.setAccountName(traderAccountName);
                account.setAuthenticated(true);
                account.setConnectionStatus(AccountStatus.AUTHENTICATED);
                accountRepository.save(account);
                log.info("Trader account authenticated for account: {}", accountId);
            }
        });

        return future;
    }
    public AccountEntity createAccount(AccountRequestDTO accountDTO) {
        BotEntity bot = botRepository.findById(accountDTO.getBotId())
                .orElseThrow(() -> new RuntimeException("Bot not found"));

        AccountEntity account = AccountEntity.builder()
                .accountName(accountDTO.getName())
                .clientId(accountDTO.getClientId())
                .clientSecret(accountDTO.getSecretId())
                .accessToken(accountDTO.getAccessToken())
                .refreshToken("refresh_token") // This should come from token response
                .tokenExpiry(DateUtil.plusDate(30))
                .isActive(accountDTO.isActive())
                .typeAccount(accountDTO.getTypeAccount())
                .isConnected(false)
                .authenticated(false)
                .bot(bot)
                .build();

        AccountEntity savedAccount = accountRepository.save(account);

        // If account is active, establish connection
        if (savedAccount.isActive()) {
            connectionService.connectAccount(savedAccount);
        }

        return savedAccount;
    }

    public AccountEntity updateAccount(Long id, AccountRequestDTO accountDTO) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        boolean wasActive = account.isActive();

        // Update fields
        account.setActive(accountDTO.isActive());

        if (accountDTO.getBotId() != null && !accountDTO.getBotId().equals(account.getBot().getId())) {
            BotEntity newBot = botRepository.findById(accountDTO.getBotId())
                    .orElseThrow(() -> new RuntimeException("Bot not found"));
            account.setBot(newBot);
        }

        AccountEntity savedAccount = accountRepository.save(account);

        // Handle connection changes based on active status
        if (!wasActive && savedAccount.isActive()) {
            // Account was activated, connect it
            connectionService.connectAccount(savedAccount);
        } else if (wasActive && !savedAccount.isActive()) {
            // Account was deactivated, disconnect it
            connectionService.disconnectAccount(savedAccount);
        }

        return savedAccount;
    }

    public void deleteAccount(Long id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Disconnect if connected
        if (account.isConnected()) {
            connectionService.disconnectAccount(account);
        }

        accountRepository.delete(account);
    }

    public AccountEntity getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<AccountEntity> getAccountsByBotId(Long botId) {
        return accountRepository.findByBotId(botId);
    }
}