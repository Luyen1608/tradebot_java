package luyen.tradebot.Trade.service;

import luyen.tradebot.Trade.dto.request.BotSupabaseDTO;
import luyen.tradebot.Trade.model.BotsEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BotsService {
    /**
     * Create a new bot from BotSupabaseDTO
     * 
     * @param botSupabaseDTO the DTO containing bot information
     * @return the created BotsEntity
     */
    BotsEntity createBot(BotSupabaseDTO botSupabaseDTO);
    
    /**
     * Get a bot by its ID
     * 
     * @param id the bot ID
     * @return an Optional containing the bot if found
     */
    Optional<BotsEntity> getBotById(UUID id);
    
    /**
     * Get all bots
     * 
     * @return a list of all bots
     */
    List<BotsEntity> getAllBots();
    
    /**
     * Update an existing bot
     * 
     * @param id the ID of the bot to update
     * @param botSupabaseDTO the DTO containing updated bot information
     * @return the updated BotsEntity
     */
    BotsEntity updateBot(UUID id, BotSupabaseDTO botSupabaseDTO);
    
    /**
     * Delete a bot by its ID
     * 
     * @param id the ID of the bot to delete
     */
    void deleteBot(UUID id);
}