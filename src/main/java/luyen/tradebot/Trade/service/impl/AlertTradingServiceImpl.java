package luyen.tradebot.Trade.service.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.model.AlertTradingEntity;
import luyen.tradebot.Trade.repository.AlertTradingRepository;
import luyen.tradebot.Trade.service.AlertTradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AlertTradingServiceImpl implements AlertTradingService {

    private final AlertTradingRepository alertTradingRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public AlertTradingServiceImpl(AlertTradingRepository alertTradingRepository, RestTemplate restTemplate) {
        this.alertTradingRepository = alertTradingRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Page<AlertTradingEntity> getAlertTradings(int pageNo, int pageSize, String search, String sorts, LocalDateTime timestamp) {
//        Pageable pageable = PageRequest.of(pageNo, pageSize);
//        return alertTradingRepository.findAll(pageable, search, sorts, timestamp);
        return null;
    }
    @Override
    public void saveAndSyncAlert(AlertTradingEntity alertTradingEntity) {
        if (alertTradingEntity == null) {
            log.error("Error: Cannot sync null AlertTradingEntity");
            return;
        }
        
        // We assume the entity is already saved in the database
        // Just sync it with the external service
        log.info("Syncing AlertTradingEntity with ID: {}", alertTradingEntity.getId());
        
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxidmh5d3h2dGh3Z2llYmpzcmxyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM3MzE4MjYsImV4cCI6MjA1OTMwNzgyNn0.SriuXDKuf0URQfJbAqQO-4r8Ghg0KVOWV6Lq99u86hU");
            headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxidmh5d3h2dGh3Z2llYmpzcmxyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM3MzE4MjYsImV4cCI6MjA1OTMwNzgyNn0.SriuXDKuf0URQfJbAqQO-4r8Ghg0KVOWV6Lq99u86hU");

            // Create a simplified payload that matches what the API expects
            // This avoids potential serialization issues with the JPA entity
            var payload = new AlertTradingPayload(
                alertTradingEntity.getId().toString(),
                alertTradingEntity.getAction() != null ? alertTradingEntity.getAction().toString() : null,
                alertTradingEntity.getInstrument(),
                alertTradingEntity.getTimestamp(),
                alertTradingEntity.getSignalToken(),
                alertTradingEntity.getMaxLag(),
                alertTradingEntity.getInvestmentType(),
                alertTradingEntity.getAmount(),
                alertTradingEntity.getStatus(),
                alertTradingEntity.getCreateAt(),
                alertTradingEntity.getUpdateAt()
            );

            // Create a custom ObjectMapper for proper serialization
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            
            try {
                // Convert payload to JSON string
                String jsonPayload = mapper.writeValueAsString(payload);
                
                // Create the request entity with the JSON string
                HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
    
                // Debug log the payload being sent
                log.info("Sending payload to Supabase: {}", payload);
                
                // Debug the actual JSON that will be sent
                log.debug("JSON payload: {}", jsonPayload);
                
                // Call the API
                String apiUrl = "https://lbvhywxvthwgiebjsrlr.supabase.co/functions/v1/sync-tbl_alert_trading";
                try {
                    ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);
    
                    // Handle the response if needed
                    if (response.getStatusCode().is2xxSuccessful()) {
                        // Successfully synced
                        log.info("Alert synced successfully.");
                        log.debug("Response body: {}", response.getBody());
                    } else {
                        // Handle error
                        log.error("Failed to sync alert: {}", response.getStatusCode());
                        log.error("Response body: {}", response.getBody());
                    }
                } catch (org.springframework.web.client.HttpClientErrorException e) {
                    // Specific handling for HTTP client errors (4xx)
                    log.error("HTTP Client Error: {}", e.getStatusCode());
                    log.error("Response body: {}", e.getResponseBodyAsString());
                    log.error("Request headers: {}", headers);
                    // Don't rethrow - we want to continue even if sync fails
                }
            } catch (JsonProcessingException e) {
                log.error("Error serializing payload to JSON: {}", e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            // Log the exception
            log.error("Error syncing alert: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Inner class to represent the payload for the API
    private static class AlertTradingPayload {
        @JsonProperty("id_be")
        private final String idBe;
        
        private final String action;
        private final String instrument;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private final LocalDateTime timestamp;
        
        @JsonProperty("signal_token")
        private final String signalToken;
        
        @JsonProperty("max_lag")
        private final String maxLag;
        
        @JsonProperty("investment_type")
        private final String investmentType;
        
        private final Double amount;
        private final String status;
        
        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime createdAt;
        
        @JsonProperty("updated_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime updatedAt;

        public AlertTradingPayload(String id, String action, String instrument, LocalDateTime timestamp,
                                  String signalToken, String maxLag, String investmentType, 
                                  Double amount, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.idBe = id;
            this.action = action;
            this.instrument = instrument;
            this.timestamp = timestamp;
            this.signalToken = signalToken;
            this.maxLag = maxLag;
            this.investmentType = investmentType;
            this.amount = amount;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters needed for JSON serialization
        @JsonProperty("id_be")
        public String getIdBe() { return idBe; }
        
        public String getAction() { return action; }
        public String getInstrument() { return instrument; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        @JsonProperty("signal_token")
        public String getSignalToken() { return signalToken; }
        
        @JsonProperty("max_lag")
        public String getMaxLag() { return maxLag; }
        
        @JsonProperty("investment_type")
        public String getInvestmentType() { return investmentType; }
        
        public Double getAmount() { return amount; }
        public String getStatus() { return status; }
        
        @JsonProperty("created_at")
        public LocalDateTime getCreatedAt() { return createdAt; }
        
        @JsonProperty("updated_at")
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        
        @Override
        public String toString() {
            return "AlertTradingPayload{" +
                    "id_be='" + idBe + '\'' +
                    ", action='" + action + '\'' +
                    ", instrument='" + instrument + '\'' +
                    ", timestamp=" + timestamp +
                    ", signal_token='" + signalToken + '\'' +
                    ", max_lag='" + maxLag + '\'' +
                    ", investment_type='" + investmentType + '\'' +
                    ", amount=" + amount +
                    ", status='" + status + '\'' +
                    ", created_at=" + createdAt +
                    ", updated_at=" + updatedAt +
                    '}';
        }
    }


}
