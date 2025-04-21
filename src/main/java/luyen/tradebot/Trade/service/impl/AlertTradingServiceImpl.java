package luyen.tradebot.Trade.service.impl;

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
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return alertTradingRepository.findAll(pageable, search, sorts, timestamp);
    }
    @Override
    public void saveAndSyncAlert(AlertTradingEntity alertTradingEntity) {
        // Save the AlertTradingEntity
        AlertTradingEntity savedEntity = alertTradingRepository.save(alertTradingEntity);

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxidmh5d3h2dGh3Z2llYmpzcmxyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM3MzE4MjYsImV4cCI6MjA1OTMwNzgyNn0.SriuXDKuf0URQfJbAqQO-4r8Ghg0KVOWV6Lq99u86hU");
        headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxidmh5d3h2dGh3Z2llYmpzcmxyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM3MzE4MjYsImV4cCI6MjA1OTMwNzgyNn0.SriuXDKuf0URQfJbAqQO-4r8Ghg0KVOWV6Lq99u86hU");

        // Create the request entity
        HttpEntity<AlertTradingEntity> requestEntity = new HttpEntity<>(savedEntity, headers);

        // Call the API
        String apiUrl = "https://lbvhywxvthwgiebjsrlr.supabase.co/functions/v1/sync-tbl_alert_trading";
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        // Handle the response if needed
        if (response.getStatusCode().is2xxSuccessful()) {
            // Successfully synced
            System.out.println("Alert synced successfully.");
        } else {
            // Handle error
            System.err.println("Failed to sync alert: " + response.getStatusCode());
        }

    }


}
