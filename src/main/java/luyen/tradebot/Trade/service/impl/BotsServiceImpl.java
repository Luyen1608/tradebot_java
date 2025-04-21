package luyen.tradebot.Trade.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.BotSupabaseDTO;
import luyen.tradebot.Trade.exception.ResourceNotFoundException;
import luyen.tradebot.Trade.mapper.BotsMapper;
import luyen.tradebot.Trade.model.BotsEntity;
import luyen.tradebot.Trade.repository.BotsRepository;
import luyen.tradebot.Trade.service.BotsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j(topic = "BOTS-SERVICE")
@RequiredArgsConstructor
public class BotsServiceImpl implements BotsService {

    private final BotsRepository botsRepository;
    private final BotsMapper botsMapper;

    @Override
    @Transactional
    public BotsEntity createBot(BotSupabaseDTO botSupabaseDTO) {
        try {
            log.info("Creating new bot with name: {}", botSupabaseDTO.getName());

            // Check if a bot with the same name already exists
            botsRepository.findByName(botSupabaseDTO.getName())
                    .ifPresent(existingBot -> {
                        throw new IllegalArgumentException("Bot with name " + botSupabaseDTO.getName() + " already exists");
                    });

            // Convert DTO to entity
            BotsEntity botsEntity = botsMapper.toEntity(botSupabaseDTO);

            // Save the entity
            BotsEntity savedEntity = botsRepository.save(botsEntity);
            log.info("Bot created successfully with ID: {}", savedEntity.getId());
            return savedEntity;
        } catch (Exception e) {
            log.error("Error creating bot:", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<BotsEntity> getBotById(UUID id) {
        log.info("Fetching bot with ID: {}", id);
        return botsRepository.findById(id);
    }

    @Override
    public List<BotsEntity> getAllBots() {
        log.info("Fetching all bots");
        return botsRepository.findAll();
    }

    @Override
    @Transactional
    public BotsEntity updateBot(UUID id, BotSupabaseDTO botSupabaseDTO) {
        log.info("Updating bot with ID: {}", id);

        // Find the existing bot
        BotsEntity existingBot = botsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bot not found with ID: " + id));

        // Update the entity with DTO data
        BotsEntity updatedBot = botsMapper.updateEntityFromDto(existingBot, botSupabaseDTO);

        // Save the updated entity
        BotsEntity savedEntity = botsRepository.save(updatedBot);
        log.info("Bot updated successfully with ID: {}", savedEntity.getId());

        return savedEntity;
    }

    @Override
    @Transactional
    public void deleteBot(UUID id) {
        log.info("Deleting bot with ID: {}", id);

        // Check if the bot exists
        if (!botsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bot not found with ID: " + id);
        }

        // Delete the bot
        botsRepository.deleteById(id);
        log.info("Bot deleted successfully with ID: {}", id);
    }
}