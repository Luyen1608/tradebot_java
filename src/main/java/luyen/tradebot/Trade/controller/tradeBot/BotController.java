package luyen.tradebot.Trade.controller.tradeBot;


import lombok.RequiredArgsConstructor;
import luyen.tradebot.Trade.dto.request.BotRequestDTO;
import luyen.tradebot.Trade.model.BotEntity;
import luyen.tradebot.Trade.service.BotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bot_bk")
@RequiredArgsConstructor
public class BotController {
    private final BotService botService;

    @PostMapping
    public ResponseEntity<BotEntity> createBot(@RequestBody BotRequestDTO botDTO) {
        BotEntity newBot = botService.createBot(botDTO);
        return new ResponseEntity<>(newBot, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBot(@PathVariable UUID id, @RequestBody BotRequestDTO botDTO) {
        UUID updatedBot = botService.updateBot(id, botDTO);
        return ResponseEntity.ok(updatedBot);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBot(@PathVariable UUID id) {
        botService.deleteBot(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BotEntity> getBot(@PathVariable UUID id) {
        BotEntity bot = botService.getBot(id);
        return ResponseEntity.ok(bot);
    }

    @GetMapping
    public ResponseEntity<List<BotEntity>> getAllBots() {
        List<BotEntity> bots = botService.getAllBots();
        return ResponseEntity.ok(bots);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BotEntity>> getActiveBots() {
        List<BotEntity> bots = botService.getActiveBots();
        return ResponseEntity.ok(bots);
    }
}