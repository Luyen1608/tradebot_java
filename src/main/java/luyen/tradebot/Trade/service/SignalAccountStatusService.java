package luyen.tradebot.Trade.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.model.OrderPosition;
import luyen.tradebot.Trade.util.enumTraderBot.Symbol;
import luyen.tradebot.Trade.util.enumTraderBot.TradeSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@Slf4j
public class SignalAccountStatusService {

    private final RestTemplate restTemplate;
    private static final Logger heartbeatLogger = LoggerFactory.getLogger("luyen.tradebot.Trade.service.CTraderConnection.HEARTBEAT");
    private final String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxidmh5d3h2dGh3Z2llYmpzcmxyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM3MzE4MjYsImV4cCI6MjA1OTMwNzgyNn0.SriuXDKuf0URQfJbAqQO-4r8Ghg0KVOWV6Lq99u86hU";

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public void sendSignalAccountStatus(OrderPosition orderPosition, String tradeSide, String symbol, String ctidTraderAccountId) {
        // log
        heartbeatLogger.info("Sending signal account status for order position ID : {}", orderPosition.getId());
        String apiUrl = "https://lbvhywxvthwgiebjsrlr.supabase.co/functions/v1/sync_signal_account_status";

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);
        headers.set("Authorization", "Bearer " + apiKey);

        // Create a custom ObjectMapper for proper serialization
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try {
            if (orderPosition.getId() == null) {
                heartbeatLogger.error("OrderPosition ID is null. Cannot send signal account status.");
                return;
            }
            double originalVolume = (double) orderPosition.getOriginalVolume();
            double volumeSent = (double) orderPosition.getVolumeSent();
            double volumeMultiplier = orderPosition.getVolumeMultiplier();
            String signalId = orderPosition.getId().toString();
            SignalAccountStatusPayload payload = new SignalAccountStatusPayload(
                    signalId,
                    orderPosition.getId().toString(),
                    orderPosition.getId().toString(),
                    tradeSide,
                    symbol,
                    originalVolume,
                    volumeSent,
                    volumeMultiplier,
                    String.valueOf(orderPosition.getOrderCtraderId()),
                    orderPosition.getStatus(),
                    orderPosition.getCreateAt().toString(),
                    orderPosition.getErrorMessage(),
                    ctidTraderAccountId,
                    orderPosition.getStopLoss().toString(),
                    orderPosition.getTakeProfit().toString(),
                    orderPosition.getRelativeStopLoss().toString(),
                    orderPosition.getRelativeTakeProfit().toString()

            );
            // Convert payload to JSON string
            String jsonPayload = mapper.writeValueAsString(payload);

            // Create the request entity with the JSON string
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

            // Debug log the payload being sent
            heartbeatLogger.info("Sending signal account status to Supabase: {}", jsonPayload);

            // Call the API
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

            // Handle the response if needed
            if (response.getStatusCode().is2xxSuccessful()) {
                heartbeatLogger.info("Signal account status synced successfully.");
                log.debug("Response body: {}", response.getBody());
            } else {
                heartbeatLogger.error("Failed to sync signal account status: {}", response.getStatusCode());
                heartbeatLogger.error("Response body: {}", response.getBody());
            }
        } catch (JsonProcessingException e) {
            heartbeatLogger.error("Error serializing payload to JSON: {}", e.getMessage());
            e.printStackTrace();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Specific handling for HTTP client errors (4xx)
            heartbeatLogger.error("HTTP Client Error: {}", e.getStatusCode());
            heartbeatLogger.error("Response body: {}", e.getResponseBodyAsString());
            heartbeatLogger.error("Request headers: {}", headers);
        } catch (Exception e) {
            heartbeatLogger.error("Error syncing signal account status: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    // Inner class to represent the payload for the API
    public static class SignalAccountStatusPayload {
        @JsonProperty("signal_id")
        private final String signalId;

        @JsonProperty("trading_account_id")
        private final String tradingAccountId;

        @JsonProperty("api_connection_id")
        private final String apiConnectionId;

        private final String action;
        private final String symbol;

        @JsonProperty("original_volume")
        private final Double originalVolume;

        @JsonProperty("volume_sent")
        private final Double volumeSent;

        @JsonProperty("volume_multiplier")
        private final Double volumeMultiplier;

        @JsonProperty("order_id")
        private final String orderId;

        private final String status;

        @JsonProperty("response_time")
        private final String responseTime;

        @JsonProperty("error_message")
        private final String errorMessage;

        @JsonProperty("accountid_trading")
        private final String accountidTrading;

        @JsonProperty("stop_loss")
        private final String stopLoss;
        @JsonProperty("take_profit")
        private final String takeProfit;
        @JsonProperty("relative_stop_loss")
        private final String relativeStopLoss;
        @JsonProperty("relative_take_profit")
        private final String relativeTakeProfit;

        public SignalAccountStatusPayload(String signalId, String tradingAccountId, String apiConnectionId, String action,
                                          String symbol, double originalVolume, double volumeSent, double volumeMultiplier,
                                          String orderId, String status, String responseTime, String errorMessage,
                                          String accountidTrading, String stopLoss, String takeProfit, String relativeStopLoss, String relativeTakeProfit) {
            this.signalId = signalId != null ? signalId : UUID.randomUUID().toString();
            this.tradingAccountId = tradingAccountId;
            this.apiConnectionId = apiConnectionId;
            this.action = action;
            this.symbol = symbol;
            this.originalVolume = originalVolume;
            this.volumeSent = volumeSent;
            this.volumeMultiplier = volumeMultiplier;
            this.orderId = orderId;
            this.status = status;
            this.responseTime = responseTime;
            this.errorMessage = errorMessage;
            this.accountidTrading = accountidTrading;
            this.stopLoss = stopLoss;
            this.takeProfit = takeProfit;
            this.relativeStopLoss = relativeStopLoss;
            this.relativeTakeProfit = relativeTakeProfit;
        }

        // Getters needed for JSON serialization
        @JsonProperty("signal_id")
        public String getSignalId() {
            return signalId;
        }

        @JsonProperty("trading_account_id")
        public String getTradingAccountId() {
            return tradingAccountId;
        }

        @JsonProperty("api_connection_id")
        public String getApiConnectionId() {
            return apiConnectionId;
        }


        @JsonProperty("original_volume")
        public Double getOriginalVolume() {
            return originalVolume;
        }

        @JsonProperty("volume_sent")
        public Double getVolumeSent() {
            return volumeSent;
        }

        @JsonProperty("volume_multiplier")
        public Double getVolumeMultiplier() {
            return volumeMultiplier;
        }

        @JsonProperty("order_id")
        public String getOrderId() {
            return orderId;
        }

        public String getStatus() {
            return status;
        }

        @JsonProperty("response_time")
        public String getResponseTime() {
            return responseTime;
        }

        @JsonProperty("error_message")
        public String getErrorMessage() {
            return errorMessage;
        }

        @JsonProperty("accountid_trading")
        public String getAccountidTrading() {
            return accountidTrading;
        }

        @Override
        public String toString() {
            return "SignalAccountStatusPayload{" +
                    "signal_id='" + signalId + '\'' +
                    ", trading_account_id='" + tradingAccountId + '\'' +
                    ", api_connection_id='" + apiConnectionId + '\'' +
                    ", action='" + action + '\'' +
                    ", symbol='" + symbol + '\'' +
                    ", original_volume=" + originalVolume +
                    ", volume_sent=" + volumeSent +
                    ", volume_multiplier=" + volumeMultiplier +
                    ", order_id='" + orderId + '\'' +
                    ", status='" + status + '\'' +
                    ", response_time='" + responseTime + '\'' +
                    ", error_message='" + errorMessage + '\'' +
                    ", accountid_trading='" + accountidTrading + '\'' +
                    ", stop_loss='" + stopLoss + '\'' +
                    ", take_profit='" + takeProfit + '\'' +
                    ", relative_stop_loss='" + relativeStopLoss + '\'' +
                    ", relative_take_profit='" + relativeTakeProfit + '\'' +
                    '}';
        }
    }

    // Helper method to create a payload with 'pending' status
    public static SignalAccountStatusPayload createPendingStatusPayload(
            String signalId,
            String tradingAccountId,
            String apiConnectionId,
            String action,
            String symbol,
            Double originalVolume,
            Double volumeMultiplier,
            String accountidTrading,String stopLoss,String takeProfit,String relativeStopLoss,String relativeTakeProfit) {

        // Calculate volume sent based on original volume and multiplier
        Double volumeSent = (originalVolume != null && volumeMultiplier != null)
                ? originalVolume * volumeMultiplier
                : null;

        return new SignalAccountStatusPayload(
                signalId,
                tradingAccountId != null ? tradingAccountId : signalId,
                apiConnectionId != null ? apiConnectionId : signalId,
                action,
                symbol,
                originalVolume != null ? originalVolume : 0.0,
                volumeSent,
                volumeMultiplier != null ? volumeMultiplier : 1.0,
                null, // order_id is null for pending status
                "pending", // status is always 'pending'
                null, // response_time is null initially
                null, // error_message is null initially
                accountidTrading,
                stopLoss,takeProfit,relativeStopLoss,relativeTakeProfit
        );
    }

    public SignalAccountStatusService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Inner class to represent the payload for the API
}
