package luyen.tradebot.Trade.mapper;

import luyen.tradebot.Trade.dto.request.BotSupabaseDTO;
import luyen.tradebot.Trade.model.BotsEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BotsMapper {
    
    /**
     * Convert BotSupabaseDTO to BotsEntity for creating a new bot
     * 
     * @param dto the BotSupabaseDTO
     * @return a new BotsEntity
     */
    public BotsEntity toEntity(BotSupabaseDTO dto) {
        BotsEntity entity = BotsEntity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .type(dto.getType())
                .risk(dto.getRisk())
                .signalToken(dto.getSignalToken())
                .webhookUrl(dto.getWebhookUrl())
                .botId(dto.getBotId())
                .isDeleted(dto.getIsDeleted())
                .ownerId(dto.getOwnerId())
                .isBestSeller(dto.getIsBestSeller())
                .build();
        entity.setId(dto.getId());

        return entity;
    }
    
    /**
     * Update an existing BotsEntity with data from BotSupabaseDTO
     * 
     * @param entity the existing BotsEntity
     * @param dto the BotSupabaseDTO with updated data
     * @return the updated BotsEntity
     */
    public BotsEntity updateEntityFromDto(BotsEntity entity, BotSupabaseDTO dto) {
        //check dto.getname is value

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        entity.setType(dto.getType());
        entity.setRisk(dto.getRisk());
        entity.setSignalToken(dto.getSignalToken());
        entity.setWebhookUrl(dto.getWebhookUrl());
        entity.setBotId(dto.getBotId());
        entity.setIsDeleted(dto.getIsDeleted());
        entity.setOwnerId(dto.getOwnerId());
        entity.setIsBestSeller(dto.getIsBestSeller());
        return entity;
    }
    
    /**
     * Convert BotsEntity to BotSupabaseDTO
     * 
     * @param entity the BotsEntity
     * @return a BotSupabaseDTO
     */
    public BotSupabaseDTO toDto(BotsEntity entity) {
        return BotSupabaseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .type(entity.getType())
                .risk(entity.getRisk())
                .signalToken(entity.getSignalToken())
                .webhookUrl(entity.getWebhookUrl())
                .botId(entity.getBotId())
                .isDeleted(entity.getIsDeleted())
                .ownerId(entity.getOwnerId())
                .isBestSeller(entity.getIsBestSeller())
                .createdAt(entity.getCreateAt())
                .updatedAt(entity.getUpdateAt())
                .build();
    }
}