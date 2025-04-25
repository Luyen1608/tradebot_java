package luyen.tradebot.Trade.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.AccountRequestDTO;
import luyen.tradebot.Trade.dto.request.AccountSupabaseDTO;
import luyen.tradebot.Trade.dto.respone.ResponseCtraderDTO;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.BotEntity;
import luyen.tradebot.Trade.model.BotsEntity;
import luyen.tradebot.Trade.model.ConnectedEntity;
import luyen.tradebot.Trade.repository.AccountRepository;
import luyen.tradebot.Trade.repository.BotRepository;
import luyen.tradebot.Trade.repository.BotsRepository;
import luyen.tradebot.Trade.repository.ConnectedRepository;
import luyen.tradebot.Trade.util.DateUtil;
import luyen.tradebot.Trade.util.ValidateRepsone;
import luyen.tradebot.Trade.util.enumTraderBot.AccountStatus;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;
import luyen.tradebot.Trade.util.enumTraderBot.ConnectStatus;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    private final ConnectedRepository connectedRepository;

    private final BotsRepository botsRepository;
    private final CTraderApiService cTraderApiService;
    private final CTraderConnectionService connectionService;


    public String getAccessToken(String clientId, String clientSecret) {
        return cTraderApiService.getAccessToken(clientId, clientSecret);
    }

    public List<Map<String, Object>> getAccounts(String accessToken) {
        return cTraderApiService.getAccounts(accessToken);
    }

    public CompletableFuture<String> getTraderAccounts(UUID accountId) {
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

    public CompletableFuture<String> authenticateTraderAccount(UUID accountId, int ctidTraderAccountId,
                                                               int traderLogin, String type, String traderAccountName) {
//        AccountEntity account = accountRepository.findById(accountId)
//                .orElseThrow(() -> new RuntimeException("Account not found"));
//
//        if (!account.isConnected()) {
//            throw new RuntimeException("Account is not connected");
//        }
//
//        CTraderConnection connection = connectionService.getConnection(accountId);
//        if (connection == null) {
//            throw new RuntimeException("No active connection for this account");
//        }
//
//        CompletableFuture<String> future = cTraderApiService.authenticateTraderAccount(
//                connection, ctidTraderAccountId);
//        ConnectedEntity connectedEntity = connectedRepository.findByAccountId(accountId);
//        if (connectedEntity == null) {
//            connectedEntity = new ConnectedEntity();
//        }
//        //{"payloadType":2103,"clientMsgId":"cm_0c252588","payload":{"ctidTraderAccountId":42683965}}
//        ConnectedEntity finalConnectedEntity = connectedEntity;
//        future.thenAccept(success -> {
//            if (success != null) {
//                ResponseCtraderDTO responseCtraderDTO = ValidateRepsone.formatResponse(success);
//                if (responseCtraderDTO.getPayloadReponse() == 2103) {
//                    account.setCtidTraderAccountId(ctidTraderAccountId);
//                    account.setTraderLogin(traderLogin);
//                    account.setTypeAccount(AccountType.valueOf(type));
//                    account.setAuthenticated(true);
//                    account.setConnectionStatus(AccountStatus.AUTHENTICATED);
//
//                    finalConnectedEntity.setAccountName(account.getAccountName());
//                    finalConnectedEntity.setAccount(account);
//                    finalConnectedEntity.setConnectionStatus(ConnectStatus.CONNECTED);
//
//                    finalConnectedEntity.setLastConnectionTime(DateUtil.plusDate(0));
////                    connectedEntity.setBotName(account.getBot().getBotName());
//
//                    account.setConnecting(finalConnectedEntity);
//
//
//                    log.info("Trader account authenticated for account: {}", accountId);
//                } else {
//                    finalConnectedEntity.setAccountName(account.getAccountName());
//                    finalConnectedEntity.setConnectionStatus(ConnectStatus.CONNECTED);
//                    finalConnectedEntity.setBotName(account.getBot().getName());
//                    finalConnectedEntity.setErrorCode(responseCtraderDTO.getErrorCode());
//                    finalConnectedEntity.setErrorMessage(responseCtraderDTO.getDescription());
//                    account.setAuthenticated(account.isAuthenticated());
//                }
//                accountRepository.save(account);
//            }
//        });
//
//        return future;
        return null;
    }

    @Transactional
    public AccountEntity createAccount(AccountRequestDTO accountDTO) {
        BotsEntity bot = botsRepository.findById(accountDTO.getBotId())
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
//            connectionService.connectAccount(savedAccount);
        }
        return savedAccount;
    }

    public AccountEntity updateAccount(UUID id, AccountRequestDTO accountDTO) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        boolean wasActive = account.isActive();

        // Update fields
        account.setActive(accountDTO.isActive());
        account.setCtidTraderAccountId(accountDTO.getCtidTraderAccountId());

        if (accountDTO.getBotId() != null && !accountDTO.getBotId().equals(account.getBot().getId())) {
            BotsEntity newBot = botsRepository.findById(accountDTO.getBotId())
                    .orElseThrow(() -> new RuntimeException("Bot not found"));
            account.setBot(newBot);
        }

        AccountEntity savedAccount = accountRepository.save(account);

        // Handle connection changes based on active status
        if (!wasActive && savedAccount.isActive()) {
            // Account was activated, connect it
//            connectionService.connectAccount(savedAccount);
        } else if (wasActive && !savedAccount.isActive()) {
            // Account was deactivated, disconnect it
//            connectionService.disconnectAccount(savedAccount);
        }

        return savedAccount;
    }

    public void deleteAccount(UUID id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Disconnect if connected
        if (account.isConnected()) {
            connectionService.disconnectAccount(account);
        }

        accountRepository.deleteById(id);
    }

    public AccountEntity getAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<AccountEntity> getAccountsByBotId(UUID botId) {
        return accountRepository.findByBotId(botId);
    }

    @Transactional
    public AccountEntity createAccountFromSupabase(AccountSupabaseDTO accountDTO) {
        if (accountDTO.getId() == null) {
            throw new IllegalArgumentException("ID không được để trống");
        }
        BotsEntity bot = botsRepository.findById(accountDTO.getBotId())
                .orElseThrow(() -> new RuntimeException("Bot not found"));
        log.debug("Tạo tài khoản mới từ Supabase: {}", accountDTO.getClientId());

        // Kiểm tra tài khoản đã tồn tại chưa
        if (accountRepository.existsByClientId(accountDTO.getClientId())) {
            throw new IllegalArgumentException("Tài khoản với Client ID này đã tồn tại");
        }

        // Kiểm tra xem ID đã tồn tại chưa
        if (accountRepository.existsById(accountDTO.getId())) {
            throw new IllegalArgumentException("Tài khoản với ID này đã tồn tại");
        }

        // Sử dụng EntityManager để tạo và lưu entity với ID được chỉ định
        AccountEntity account = new AccountEntity();
        account.setId(accountDTO.getId());
        account.setAccountName(accountDTO.getAccountidTrading());
        try {
            account.setCtidTraderAccountId(Integer.valueOf(accountDTO.getAccountidTrading()));
        } catch (NumberFormatException e) {
            log.warn("Invalid accountid_trading format: {}", accountDTO.getAccountidTrading());
        }
        account.setClientId(accountDTO.getClientId());
        account.setClientSecret(accountDTO.getSecretId());
        account.setAccessToken(accountDTO.getAccessToken());
        account.setRefreshToken("refresh_token"); // This should come from token response
        account.setTokenExpiry(DateUtil.plusDate(30));
        account.setActive(true);
        account.setTypeAccount(AccountType.DEMO); // Default value, can be updated later
        account.setConnected(false);
        account.setAuthenticated(false);
        account.setVolumeMultiplier(accountDTO.getVolumeMultiplier());
        account.setBot(bot);

        // Set creation and update timestamps if available in the DTO
        if (accountDTO.getCreatedAt() != null) {
            account.setCreateAt(accountDTO.getCreatedAt());
        }
        if (accountDTO.getUpdatedAt() != null) {
            account.setUpdateAt(accountDTO.getUpdatedAt());
        }
        // Sử dụng saveAndFlush để đảm bảo entity được lưu ngay lập tức
        AccountEntity savedAccount = accountRepository.saveAndFlush(account);
        // Nếu account được tạo thành công và trạng thái của nó là true thì kết nối tài khoản đó
        if (savedAccount.isActive()) {
            connectionService.connectAccount(savedAccount.getId());
        }
        return savedAccount;
    }

    /**
     * Cập nhật tài khoản từ Supabase
     *
     * @param id         ID của tài khoản cần cập nhật
     * @param accountDTO DTO chứa thông tin cập nhật
     * @return Tài khoản đã được cập nhật
     */
    @Transactional
    public AccountEntity updateAccountFromSupabase(UUID id, AccountSupabaseDTO accountDTO) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));

        log.info("Updating account from Supabase with ID: {}", id);

        // Cập nhật thông tin bot nếu có thay đổi
        if (accountDTO.getBotId() != null && !accountDTO.getBotId().equals(account.getBot().getId())) {
            BotsEntity newBot = botsRepository.findById(accountDTO.getBotId())
                    .orElseThrow(() -> new RuntimeException("Bot not found with ID: " + accountDTO.getBotId()));
            account.setBot(newBot);
        }

        // Cập nhật các thông tin khác
        if (accountDTO.getAccountidTrading() != null) {
            account.setAccountName(accountDTO.getAccountidTrading());
            try {
                account.setCtidTraderAccountId(Integer.valueOf(accountDTO.getAccountidTrading()));
            } catch (NumberFormatException e) {
                log.warn("Invalid accountid_trading format: {}", accountDTO.getAccountidTrading());
            }
        }

        if (accountDTO.getClientId() != null) {
            account.setClientId(accountDTO.getClientId());
        }

        if (accountDTO.getSecretId() != null) {
            account.setClientSecret(accountDTO.getSecretId());
        }

        if (accountDTO.getAccessToken() != null) {
            account.setAccessToken(accountDTO.getAccessToken());
        }

        if (accountDTO.getVolumeMultiplier() != null) {
            account.setVolumeMultiplier(accountDTO.getVolumeMultiplier());
        }


        // Cập nhật thời gian
        if (accountDTO.getUpdatedAt() != null) {
            account.setUpdateAt(accountDTO.getUpdatedAt());
        }

        // Lưu tài khoản đã cập nhật
        AccountEntity savedAccount = accountRepository.save(account);
        log.info("Account updated successfully with ID: {}", savedAccount.getId());

        return savedAccount;
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