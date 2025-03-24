package luyen.tradebot.Trade.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.AccountRequestDTO;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.BotEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.repository.BotRepository;
import luyen.tradebot.Trade.repository.ConnectedRepository;
import luyen.tradebot.Trade.util.DateUtil;
import luyen.tradebot.Trade.util.ValidateRepsone;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;
import luyen.tradebot.Trade.util.enumTraderBot.ConnectStatus;
import luyen.tradebot.Trade.util.enumTraderBot.ConnectionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    private final ConnectedRepository connectedRepository;

    private final BotRepository botRepository;
    private final CTraderApiService cTraderApiService;
    private final CTraderConnectionService connectionService;


    public String getAccessToken(String clientId, String clientSecret) {
        return cTraderApiService.getAccessToken(clientId, clientSecret);
    }

    public List<Map<String, Object>> getAccounts(String accessToken) {
        return cTraderApiService.getAccounts(accessToken);
    }

    public CompletableFuture<String> getTraderAccounts(Long accountId) {
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

    public CompletableFuture<String> authenticateTraderAccount(Long accountId, int ctidTraderAccountId, String traderAccountName) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.isConnected()) {
            throw new RuntimeException("Account is not connected");
        }

        CTraderConnection connection = connectionService.getConnection(accountId);
        if (connection == null) {
            throw new RuntimeException("No active connection for this account");
        }

        CompletableFuture<String> future = cTraderApiService.authenticateTraderAccount(
                connection, ctidTraderAccountId);
        ConnectedEntity connectedEntity = connectedRepository.findByAccountId(accountId);
        if (connectedEntity == null) {
            connectedEntity = new ConnectedEntity();
        }
        //{"payloadType":2103,"clientMsgId":"cm_0c252588","payload":{"ctidTraderAccountId":42683965}}
        ConnectedEntity finalConnectedEntity = connectedEntity;
        future.thenAccept(success -> {
            if (success !=null) {
                ResponseCtraderDTO responseCtraderDTO = ValidateRepsone.formatResponse(success);
                if (responseCtraderDTO.getPayloadReponse() ==2103){
                    account.setCtidTraderAccountId(ctidTraderAccountId);
                    account.setAuthenticated(true);
                    account.setConnectionStatus(AccountStatus.AUTHENTICATED);

                    finalConnectedEntity.setAccount(account);
                    finalConnectedEntity.setConnectionStatus(ConnectStatus.CONNECTED);
                    finalConnectedEntity.setAccountName(account.getAccountName());
                    finalConnectedEntity.setLastConnectionTime(DateUtil.plusDate(0));
//                    connectedEntity.setBotName(account.getBot().getBotName());

                    account.setConnecting(finalConnectedEntity);


                    log.info("Trader account authenticated for account: {}", accountId);
                } else {
                    finalConnectedEntity.setAccountName(account.getAccountName());
                    finalConnectedEntity.setConnectionStatus(ConnectStatus.CONNECTED);
                    finalConnectedEntity.setBotName(account.getBot().getBotName());
                    finalConnectedEntity.setErrorCode(responseCtraderDTO.getErrorCode());
                    finalConnectedEntity.setErrorMessage(responseCtraderDTO.getDescription());
                    account.setAuthenticated(account.isAuthenticated());
                }
                accountRepository.save(account);
            }
        });

        return future;
    }

    @Transactional
    public AccountEntity createAccount(AccountRequestDTO accountDTO) {
        BotEntity bot = botRepository.findById(accountDTO.getBotId())
                .orElseThrow(() -> new RuntimeException("Bot not found"));
        log.debug("Tạo tài khoản mới: {}", accountDTO.getClientId());

        // Kiểm tra tài khoản đã tồn tại chưa
        if (accountRepository.existsByClientId((accountDTO.getClientId()))) {
            throw new IllegalArgumentException("Tài khoản với Client ID này đã tồn tại");
        }
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
            // Đảm bảo thêm thông tin kết nối vào ConnectionService
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
        account.setCtidTraderAccountId(accountDTO.getCtidTraderAccountId());

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

    /**
     * Chuyển đổi Entity Account sang DTO
     */
    private AccountRequestDTO convertToDTO(AccountEntity account) {
        String uptimeStr = "";
        return AccountRequestDTO.builder()
                .id(account.getId())
                .name(account.getAccountName())
                .clientId(account.getClientId())
                .status(account.getConnectionStatus())
                .accountId(account.getAccountId())
                .botId(account.getBot().getId())
                .isAuthenticated(account.isAuthenticated())
                .accessToken(account.getAccessToken())
                .typeAccount(account.getTypeAccount())
                .isConnected(account.isConnected())
                .secretId(account.getClientSecret())
                .expirationDate(account.getTokenExpiry())
                .build();
    }
}