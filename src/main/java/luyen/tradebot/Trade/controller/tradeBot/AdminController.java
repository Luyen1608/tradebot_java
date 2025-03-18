package luyen.tradebot.Trade.controller.tradeBot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.AccountRequestDTO;
import luyen.tradebot.Trade.dto.request.BotRequestDTO;
import luyen.tradebot.Trade.dto.respone.BotResponse;
import luyen.tradebot.Trade.dto.respone.ResponseData;
import luyen.tradebot.Trade.service.BotService;
import luyen.tradebot.Trade.service.CTraderConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/admin")
@AllArgsConstructor
@Slf4j
public class AdminController {

    private final CTraderConnectionManager connectionManager;

    private final BotService botService;

    @PostMapping("/bot")
    public ResponseData<?> addBot(@Valid @RequestBody BotRequestDTO request) {
        try {
            long botId = botService.saveBot(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Save Bot Successfully", botId);
        } catch (Exception e) {
            log.error("error message = {} ", e.getMessage(), e.getCause());
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }

    }

    @PatchMapping("/bot/{id}")
    public ResponseData<?> updateBot(@PathVariable("id") @Min(1) long id, @Valid @RequestBody BotRequestDTO request) {
        try {
            botService.updateBot(id, request);
            return new ResponseData<>(HttpStatus.OK.value(), "Update Bot Successfully");
        } catch (Exception e) {
            log.error("error message = {} ", e.getMessage(), e.getCause());
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    @DeleteMapping("/bot/{id}")
    public ResponseData<?> deleteBot(@PathVariable("id") @Min(1) long id) {
        try {
            botService.deleteBot(id);
            return new ResponseData<>(HttpStatus.OK.value(), "Delete Bot Successfully");
        } catch (Exception e) {
            log.error("error message = {} ", e.getMessage(), e.getCause());
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    @GetMapping("/bot/{id}")
    public ResponseData<?> getBotById(@PathVariable("id") @Min(1) long id) {
        try {
            BotResponse botResponse = botService.getBotById(id);
            return new ResponseData<>(HttpStatus.OK.value(), "Delete Bot Successfully", botResponse);
        } catch (Exception e) {
            log.error("error message = {} ", e.getMessage(), e.getCause());
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    @PostMapping("/bot/{id}/account")
    public ResponseData<?> addAccountByBotId(@PathVariable("id") @Min(1) long id, @Valid @RequestBody AccountRequestDTO request) {
        try {
            long botId = botService.saveAccount(id, request);
            return new ResponseData<>(HttpStatus.OK.value(), "Save Bot Successfully", botId);
        } catch (Exception e) {
            log.error("error message = {} ", e.getMessage(), e.getCause());
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    @PostMapping("/request-account-list")
    public CompletableFuture<String> requestAccountList(@RequestParam String accessToken) {
        return connectionManager.requestAccountList(accessToken);
    }
}
