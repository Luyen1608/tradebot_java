package luyen.tradebot.Trade.controller.tradeBot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.BotSupabaseDTO;
import luyen.tradebot.Trade.dto.request.WebhookPayload;
import luyen.tradebot.Trade.dto.respone.ApiResponse;
import luyen.tradebot.Trade.dto.respone.ResponseData;
import luyen.tradebot.Trade.mapper.BotsMapper;
import luyen.tradebot.Trade.model.BotsEntity;
import luyen.tradebot.Trade.service.BotsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpHeaders;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bots")
@RequiredArgsConstructor
@Slf4j(topic = "BOTS-CONTROLLER")
public class BotsController {

    private final BotsService botsService;
    private final BotsMapper botsMapper;

    @PostMapping
    public ResponseEntity<?> createBot(@RequestBody WebhookPayload payload) {
        log.info("Received Supabase Webhook: table={}, type={}", payload.getTable(), payload.getType());

        // Chỉ xử lý INSERT vào bot_accounts
        if ("bots".equals(payload.getTable())) {
            // 1) INSERT → tạo bot_accounts
            if ("INSERT".equals(payload.getType())) {
                log.info("Handling INSERT event for bots");
                Map<String, Object> record = payload.getRecord();
               // Xử lý owner_id có thể null
                String ownerIdStr = (String) record.get("owner_id");
                UUID ownerId = ownerIdStr != null ? UUID.fromString(ownerIdStr) : null;
                
                // Xử lý các trường UUID có thể null
                String idStr = (String) record.get("id");
                UUID id = idStr != null ? UUID.fromString(idStr) : null;
                
                String botIdStr = (String) record.get("bot_id");
                UUID botId = botIdStr != null ? UUID.fromString(botIdStr) : null;
                
                BotSupabaseDTO botSupabaseDTO = BotSupabaseDTO.builder()
                       .id(id)
                       .botId(botId)
                       .name((String) record.get("name"))
                       .description((String) record.get("description"))
                       .status((String) record.get("status"))
                       .type((String) record.get("type"))
                       .risk((String) record.get("risk"))
                       .signalToken((String) record.get("signal_token"))
                       .webhookUrl((String) record.get("webhook_url"))
                       .isDeleted(record.get("is_deleted") != null ? (Boolean) record.get("is_deleted") : false)
                       .ownerId(ownerId)
                       .isBestSeller(record.get("is_best_seller") != null ? (Boolean) record.get("is_best_seller") : false)
                       .createdAt(record.get("created_at") != null ? (LocalDateTime) record.get("created_at") : null)
                       .updatedAt(record.get("updated_at") != null ? (LocalDateTime) record.get("updated_at") : null)
                       .build();
                BotsEntity createdBot = botsService.createBot(botSupabaseDTO);
                BotSupabaseDTO responseDto = botsMapper.toDto(createdBot);
                ApiResponse<BotSupabaseDTO> response = ApiResponse.<BotSupabaseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Bot created successfully")
                        .data(responseDto)
                        .build();
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            }
            // 2) DELETE → xóa bot_accounts theo bot_ids
            else if ("DELETE".equals(payload.getType())) {
                Map<String, Object> oldRecord = payload.getOldRecord();
                if (oldRecord != null && oldRecord.get("id") != null) {
                    String botIdStr = oldRecord.get("id").toString();
                    botsService.deleteBot(UUID.fromString(botIdStr));
                    ApiResponse<BotSupabaseDTO> response = ApiResponse.<BotSupabaseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Bot deleted successfully")
                            .data(null)
                            .build();
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    ApiResponse<BotSupabaseDTO> response = ApiResponse.<BotSupabaseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Invalid delete request: missing bot ID")
                            .data(null)
                            .build();
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
            // 3) UPDATE → cập nhật bot theo id
            else if ("UPDATE".equals(payload.getType())) {
                log.info("Handling UPDATE event for bots");
                Map<String, Object> record = payload.getRecord();
                
                if (record != null && record.get("id") != null) {
                    // Xử lý các trường UUID có thể null
                    String idStr = record.get("id").toString();
                    UUID id = UUID.fromString(idStr);
                    
                    String botIdStr = (String) record.get("bot_id");
                    UUID botId = botIdStr != null ? UUID.fromString(botIdStr) : null;
                    
                    String ownerIdStr = (String) record.get("owner_id");
                    UUID ownerId = ownerIdStr != null ? UUID.fromString(ownerIdStr) : null;
                    
                    BotSupabaseDTO botSupabaseDTO = BotSupabaseDTO.builder()
                           .id(id)
                           .botId(botId)
                           .name((String) record.get("name"))
                           .description((String) record.get("description"))
                           .status((String) record.get("status"))
                           .type((String) record.get("type"))
                           .risk((String) record.get("risk"))
                           .signalToken((String) record.get("signal_token"))
                           .webhookUrl((String) record.get("webhook_url"))
                           .isDeleted(record.get("is_deleted") != null ? (Boolean) record.get("is_deleted") : false)
                           .ownerId(ownerId)
                           .isBestSeller(record.get("is_best_seller") != null ? (Boolean) record.get("is_best_seller") : false)
                           .createdAt(record.get("created_at") != null ? (LocalDateTime) record.get("created_at") : null)
                           .updatedAt(record.get("updated_at") != null ? (LocalDateTime) record.get("updated_at") : null)
                           .build();
                    
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
                        log.error("Error updating bot: ", e);
                        ApiResponse<BotSupabaseDTO> response = ApiResponse.<BotSupabaseDTO>builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message(e.getMessage())
                                .data(null)
                                .build();
                        
                        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                    }
                } else {
                    ApiResponse<BotSupabaseDTO> response = ApiResponse.<BotSupabaseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Invalid update request: missing bot ID")
                            .data(null)
                            .build();
                    
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }

        }
        // Bỏ qua các event khác
        return null;
    }


    /**
     * Create a new bot
     * 
     * @param botSupabaseDTO the DTO containing bot information
     * @return ResponseEntity with the created bot and HTTP status
     */
    @PostMapping("/testAdd")
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