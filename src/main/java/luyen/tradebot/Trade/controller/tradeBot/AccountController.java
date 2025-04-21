package luyen.tradebot.Trade.controller.tradeBot;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import luyen.tradebot.Trade.dto.request.AccountRequestDTO;
import luyen.tradebot.Trade.dto.request.AccountSupabaseDTO;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Validated
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
    public ResponseEntity<AccountEntity> createAccountFromSupabase(@RequestBody AccountSupabaseDTO accountDTO) {
        AccountEntity newAccount = accountService.createAccountFromSupabase(accountDTO);
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
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
        return accountService.authenticateTraderAccount(accountId, ctidTraderAccountId,traderLogin,type, traderAccountName)
                .thenApply(ResponseEntity::ok);
    }
}