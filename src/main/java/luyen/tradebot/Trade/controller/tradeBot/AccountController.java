package luyen.tradebot.Trade.controller.tradeBot;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.AccountRequestDTO;
import luyen.tradebot.Trade.dto.request.AccountSupabaseDTO;
import luyen.tradebot.Trade.dto.request.BotSupabaseDTO;
import luyen.tradebot.Trade.dto.request.WebhookPayload;
import luyen.tradebot.Trade.dto.respone.ApiResponse;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.BotsEntity;
import luyen.tradebot.Trade.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Account Controller")
public class AccountController {
    private final AccountService accountService;


    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> getAccessToken(
            @RequestParam String clientId,
            @RequestParam String clientSecret) {
        String accessToken = accountService.getAccessToken(clientId, clientSecret);
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getAccounts(
            @RequestParam String accessToken) {
        List<Map<String, Object>> accounts = accountService.getAccounts(accessToken);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<AccountEntity> createAccount(@RequestBody AccountRequestDTO accountDTO) {
        AccountEntity newAccount = accountService.createAccount(accountDTO);
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }

    @PostMapping("/supabase")
    public ResponseEntity<?> createAccountFromSupabase(@RequestBody WebhookPayload payload) {
        if ("bot_accounts".equals(payload.getTable())) {
            // 1) INSERT → tạo bot_accounts
            if ("INSERT".equals(payload.getType())) {
                log.info("Handling INSERT event for bot_accounts");
                Map<String, Object> record = payload.getRecord();
                AccountSupabaseDTO accountDTO = AccountSupabaseDTO.builder()
                        .id(UUID.fromString(record.get("id").toString()))
                        .botId(UUID.fromString((String) record.get("bot_id")))
                        .accountidTrading((String) record.get("accountid_trading"))
                        .userId(UUID.fromString((String) record.get("user_id")))
                        .addedDate((LocalDateTime) record.get("added_date"))
                        .status((String) record.get("status"))
                        .volumeMultiplier(Double.parseDouble(record.get("volume_multiplier").toString()))
                        .apiConnectId(UUID.fromString((String) record.get("api_connection_id")))
                        .signalToken((String) record.get("signal_token"))
                        .clientId((String) record.get("client_id"))
                        .secretId((String) record.get("secret_id"))
                        .accessToken((String) record.get("access_token"))
                        .live((Boolean) record.get("live"))
                        .createdAt((LocalDateTime) record.get("created_at"))
                        .updatedAt((LocalDateTime) record.get("updated_at"))
                        .build();
                AccountEntity newAccount = accountService.createAccountFromSupabase(accountDTO);
                return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
            }
            // 2) DELETE → xóa bot_accounts theo bot_ids
            else if ("DELETE".equals(payload.getType())) {
                String botId = payload.getOldRecord().get("id").toString();
                accountService.deleteAccount(UUID.fromString(botId));
                return new ResponseEntity<>("Bots Deleted", HttpStatusCode.valueOf(HttpStatus.NO_CONTENT.value()));
            }
        }
        return ResponseEntity.badRequest().body("Invalid event");
    }

    @GetMapping("/{accountId}/trader-accounts")
    public CompletableFuture<ResponseEntity<String>> getTraderAccounts(
            @PathVariable UUID accountId) {
        return accountService.getTraderAccounts(accountId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(500).body("⚠ Error: " + ex.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountEntity> updateAccount(@PathVariable UUID id, @RequestBody AccountRequestDTO accountDTO) {
        AccountEntity updatedAccount = accountService.updateAccount(id, accountDTO);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountEntity> getAccount(@PathVariable UUID id) {
        AccountEntity account = accountService.getAccount(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    public ResponseEntity<List<AccountEntity>> getAllAccounts() {
        List<AccountEntity> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/bot/{botId}")
    public ResponseEntity<List<AccountEntity>> getAccountsByBotId(@PathVariable UUID botId) {
        List<AccountEntity> accounts = accountService.getAccountsByBotId(botId);
        return ResponseEntity.ok(accounts);
    }


    @PostMapping("/{accountId}/authenticate")
    public CompletableFuture<ResponseEntity<String>> authenticateTraderAccount(
            @PathVariable UUID accountId,
            @RequestParam int ctidTraderAccountId,
            @RequestParam int traderLogin,
            @RequestParam String type,
            @RequestParam(required = false) String traderAccountName) {
        return accountService.authenticateTraderAccount(accountId, ctidTraderAccountId, traderLogin, type, traderAccountName)
                .thenApply(ResponseEntity::ok);
    }
}