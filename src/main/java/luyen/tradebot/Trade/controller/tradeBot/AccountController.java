package luyen.tradebot.Trade.controller.tradeBot;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.*;
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

                // Xử lý các trường UUID có thể null
                String idStr = record.get("id") != null ? record.get("id").toString() : null;
                UUID id = idStr != null ? UUID.fromString(idStr) : null;

                String botIdStr = (String) record.get("bot_id");
                UUID botId = botIdStr != null ? UUID.fromString(botIdStr) : null;

                String userIdStr = (String) record.get("user_id");
                UUID userId = userIdStr != null ? UUID.fromString(userIdStr) : null;

//                String apiConnectIdStr = (String) record.get("api_connection_id");
//                UUID apiConnectId = apiConnectIdStr != null ? UUID.fromString(apiConnectIdStr) : null;

                // Xử lý trường Double có thể null
                Double volumeMultiplier = null;
                if (record.get("volume_multiplier") != null) {
                    try {
                        volumeMultiplier = Double.parseDouble(record.get("volume_multiplier").toString());
                    } catch (NumberFormatException e) {
                        log.warn("Invalid volume_multiplier format: {}", record.get("volume_multiplier"));
                    }
                }

                AccountSupabaseDTO accountDTO = AccountSupabaseDTO.builder()
                        .id(id)
                        .botId(botId)
                        .accountidTrading((String) record.get("accountid_trading"))
                        .userId(userId)
//                        .addedDate((LocalDateTime) record.get("added_date"))
                        .status((String) record.get("status"))
                        .volumeMultiplier(volumeMultiplier)
                        .active((Boolean)record.get("active"))
//                        .apiConnectId(apiConnectId)
                        .signalToken((String) record.get("signal_token"))
                        .clientId((String) record.get("client_id"))
                        .secretId((String) record.get("secret_id"))
                        .accessToken((String) record.get("access_token"))
                        .live(record.get("live") != null ? (Boolean) record.get("live") : false)
//                        .createdAt(record.get("created_at") != null ? (LocalDateTime) record.get("created_at") : null)
//                        .updatedAt(record.get("updated_at") != null ? (LocalDateTime) record.get("updated_at") : null)
                        .build();
                AccountEntity newAccount = new AccountEntity();
                ApiResponse<AccountEntity> response = new ApiResponse<>();
                try{
                    newAccount = (AccountEntity)accountService.createAccountFromSupabase(accountDTO);
                     response = ApiResponse.<AccountEntity>builder()
                            .status(HttpStatus.CREATED.value())
                            .message("Account created successfully")
                            .data(newAccount)
                            .build();
                } catch (Exception e){
                    response = ApiResponse.<AccountEntity>builder()
                            .status(HttpStatus.CREATED.value())
                            .message(e.toString())
                            .data(newAccount)
                            .build();
                }
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else if ("DELETE".equals(payload.getType())) {
                // 2) DELETE → xóa bot_accounts theo bot_ids
                log.info("Handling DELETE event for bot_accounts");
                Map<String, Object> oldRecord = payload.getOldRecord();
                if (oldRecord != null && oldRecord.get("id") != null) {
                    String accountIdStr = oldRecord.get("id").toString();
                    accountService.deleteAccount(UUID.fromString(accountIdStr));
                    ApiResponse<AccountEntity> response = ApiResponse.<AccountEntity>builder()
                            .status(HttpStatus.OK.value())
                            .message("Account deleted successfully")
                            .data(null)
                            .build();
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    ApiResponse<AccountEntity> response = ApiResponse.<AccountEntity>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Invalid delete request: missing account ID")
                            .data(null)
                            .build();
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            } else if ("UPDATE".equals(payload.getType())) {
                // 3) UPDATE → cập nhật account theo id
                log.info("Handling UPDATE event for bot_accounts");
                Map<String, Object> record = payload.getRecord();
                Map<String, Object> oldRecord = payload.getOldRecord();
//                if (oldRecord != null && oldRecord.get("id") != null) {
//                    if (oldRecord != null && oldRecord.get("id") != null) {
//                        String accountIdOld = oldRecord.get("id").toString();
//                        accountService.deleteAccount(UUID.fromString(accountIdOld));
//                    }
//                }
                if (record != null && record.get("id") != null) {
                    // Xử lý các trường UUID có thể null
                    String idStr = record.get("id").toString();
                    UUID id = UUID.fromString(idStr);

                    String botIdStr = (String) record.get("bot_id");
                    UUID botId = botIdStr != null ? UUID.fromString(botIdStr) : null;

                    String userIdStr = (String) record.get("user_id");
                    UUID userId = userIdStr != null ? UUID.fromString(userIdStr) : null;

                    String apiConnectIdStr = (String) record.get("api_connection_id");
                    UUID apiConnectId = apiConnectIdStr != null ? UUID.fromString(apiConnectIdStr) : null;

                    // Xử lý trường Double có thể null
                    Double volumeMultiplier = null;
                    if (record.get("volume_multiplier") != null) {
                        try {
                            volumeMultiplier = Double.parseDouble(record.get("volume_multiplier").toString());
                        } catch (NumberFormatException e) {
                            log.warn("Invalid volume_multiplier format: {}", record.get("volume_multiplier"));
                        }
                    }
                    AccountSupabaseDTO accountDTO = AccountSupabaseDTO.builder()
                            .id(id)
                            .botId(botId)
                            .accountidTrading((String) record.get("accountid_trading"))
                            .userId(userId)
                            .status((String) record.get("status"))
                            .active((Boolean)record.get("active"))
                            .volumeMultiplier(volumeMultiplier)
                            .apiConnectId(apiConnectId)
                            .signalToken((String) record.get("signal_token"))
                            .clientId((String) record.get("client_id"))
                            .secretId((String) record.get("secret_id"))
                            .accessToken((String) record.get("access_token"))
                            .live(record.get("live") != null ? (Boolean) record.get("live") : false)
//                            .createdAt(record.get("created_at") != null ? (LocalDateTime) record.get("created_at") : null)
//                            .updatedAt(record.get("updated_at") != null ? (LocalDateTime) record.get("updated_at") : null)
                            .build();
                    try {
                        AccountEntity updatedAccount = accountService.updateAccountFromSupabase(id, accountDTO);
                        ApiResponse<AccountSupabaseDTO> response = ApiResponse.<AccountSupabaseDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("Account updated successfully")
                                .data(accountDTO)
                                .build();

                        return new ResponseEntity<>(response, HttpStatus.OK);
                    } catch (Exception e) {
                        log.error("Error updating account: ", e);
                        ApiResponse<AccountEntity> response = ApiResponse.<AccountEntity>builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message(e.getMessage())
                                .data(null)
                                .build();

                        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                    }
                } else {
                    ApiResponse<AccountEntity> response = ApiResponse.<AccountEntity>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Invalid update request: missing account ID")
                            .data(null)
                            .build();

                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
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
    @GetMapping("/{accountId}/get_list_current_order")
    public CompletableFuture<ResponseEntity<String>> getListCurrentOrder(
            @PathVariable UUID accountId,  @RequestParam String ctidTraderAccountId,@RequestParam String fromTimestamp,@RequestParam String toTimestamp) {
        AccountCurrentOrderDTO accountCurrentOrderDTO = AccountCurrentOrderDTO.builder()
                .ctidTraderAccountId(Integer.parseInt(ctidTraderAccountId))
                .fromTimestamp(Long.parseLong(fromTimestamp))
                .toTimestamp(Long.parseLong(toTimestamp)).build();
        return accountService.getListCurrentOrder(accountId, accountCurrentOrderDTO)
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
//        return accountService.authenticateTraderAccount(accountId, ctidTraderAccountId, traderLogin, type, traderAccountName)
//                .thenApply(ResponseEntity::ok);
        return null;
    }
}