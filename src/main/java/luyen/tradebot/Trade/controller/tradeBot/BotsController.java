package luyen.tradebot.Trade.controller.tradeBot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.BotSupabaseDTO;
import luyen.tradebot.Trade.dto.respone.ApiResponse;
import luyen.tradebot.Trade.mapper.BotsMapper;
import luyen.tradebot.Trade.model.BotsEntity;
import luyen.tradebot.Trade.service.BotsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bots")
@RequiredArgsConstructor
@Slf4j(topic = "BOTS-CONTROLLER")
public class BotsController {

    private final BotsService botsService;
    private final BotsMapper botsMapper;

    /**
     * Create a new bot
     * 
     * @param botSupabaseDTO the DTO containing bot information
     * @return ResponseEntity with the created bot and HTTP status
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BotSupabaseDTO>> createBot(@RequestBody BotSupabaseDTO botSupabaseDTO) {
        log.info("Received request to create bot: {}", botSupabaseDTO.getName());
        
        BotsEntity createdBot = botsService.createBot(botSupabaseDTO);
        BotSupabaseDTO responseDto = botsMapper.toDto(createdBot);
        
        ApiResponse<BotSupabaseDTO> response = ApiResponse.<BotSupabaseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("Bot created successfully")
                .data(responseDto)
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a bot by its ID
     * 
     * @param id the bot ID
     * @return ResponseEntity with the bot and HTTP status
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BotSupabaseDTO>> getBotById(@PathVariable UUID id) {
        log.info("Received request to get bot with ID: {}", id);
        
        return botsService.getBotById(id)
                .map(bot -> {
                    BotSupabaseDTO responseDto = botsMapper.toDto(bot);
                    ApiResponse<BotSupabaseDTO> response = ApiResponse.<BotSupabaseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Bot retrieved successfully")
                            .data(responseDto)
                            .build();
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(
                        ApiResponse.<BotSupabaseDTO>builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message("Bot not found with ID: " + id)
                                .build(),
                        HttpStatus.NOT_FOUND));
    }

    /**
     * Get all bots
     * 
     * @return ResponseEntity with the list of bots and HTTP status
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BotSupabaseDTO>>> getAllBots() {
        log.info("Received request to get all bots");
        
        List<BotsEntity> bots = botsService.getAllBots();
        List<BotSupabaseDTO> responseDtos = bots.stream()
                .map(botsMapper::toDto)
                .collect(Collectors.toList());
        
        ApiResponse<List<BotSupabaseDTO>> response = ApiResponse.<List<BotSupabaseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Bots retrieved successfully")
                .data(responseDtos)
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update an existing bot
     * 
     * @param id the ID of the bot to update
     * @param botSupabaseDTO the DTO containing updated bot information
     * @return ResponseEntity with the updated bot and HTTP status
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BotSupabaseDTO>> updateBot(
            @PathVariable UUID id,
            @RequestBody BotSupabaseDTO botSupabaseDTO) {
        log.info("Received request to update bot with ID: {}", id);
        
        try {
            BotsEntity updatedBot = botsService.updateBot(id, botSupabaseDTO);
            BotSupabaseDTO responseDto = botsMapper.toDto(updatedBot);
            
            ApiResponse<BotSupabaseDTO> response = ApiResponse.<BotSupabaseDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("Bot updated successfully")
                    .data(responseDto)
                    .build();
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<BotSupabaseDTO> response = ApiResponse.<BotSupabaseDTO>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .build();
            
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a bot by its ID
     * 
     * @param id the ID of the bot to delete
     * @return ResponseEntity with HTTP status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBot(@PathVariable UUID id) {
        log.info("Received request to delete bot with ID: {}", id);
        
        try {
            botsService.deleteBot(id);
            
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("Bot deleted successfully")
                    .build();
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .build();
            
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}