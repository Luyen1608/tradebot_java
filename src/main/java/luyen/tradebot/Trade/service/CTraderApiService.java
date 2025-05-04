package luyen.tradebot.Trade.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.request.PlaceOrderRequest;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.OrderEntity;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;
import luyen.tradebot.Trade.util.enumTraderBot.PayloadType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CTraderApiService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaProducerService kafkaProducerService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${ctrader.ws.demo.url}")
    private String CTRADER_API_BASE_URL;

    @Value("${ctrader.ws.demo.url}")
    private String CTRADER_DEMO_WS_URL;

    @Value("${ctrader.ws.live.url}")
    private String CTRADER_LIVE_WS_URL;

    @Value("${tradebot.prefix:trade365_}")
    private String prefix;
    private final Map<String, String> accountTokens = new HashMap<>(); // Lưu trữ token giả định

    public CTraderConnection connect(UUID accountId, String clientId, String clientSecret,
                                     String accessToken, AccountType accountType,
                                     CTraderConnectionService ctraderConnectionService, String wsUrl,Double volumeMultiplier,
                                     int ctidTraderAccountId) {
        // Implement WebSocket connection to cTrader API
        if (wsUrl ==null){
            wsUrl = "DEMO".equalsIgnoreCase(accountType.toString()) ?
                    CTRADER_DEMO_WS_URL : CTRADER_LIVE_WS_URL;
        }
//        CTraderConnection connection = new CTraderConnection(accountId, clientId, clientSecret, accessToken, ctraderConnectionService, wsUrl);
        CTraderConnection connection = new CTraderConnection(accountId, clientId, clientSecret, accessToken,
                  ctraderConnectionService, wsUrl, kafkaTemplate, kafkaProducerService, prefix, volumeMultiplier, ctidTraderAccountId);
        connection.connect();
        return connection;
    }

    public CompletableFuture<String> getTraderAccounts(CTraderConnection connection) {
        // Get trader accounts via WebSocket
        return connection.getAccountListByAccessToken();
    }

    public CompletableFuture<String> placeOrder(PlaceOrderRequest request) {
        // Authenticate specific trader account
        return request.getConnection().placeOrder(request);
    }

    public CompletableFuture<String> closePosition(CTraderConnection connection,
                                                   String clientMsgId, int positionId, int volume, PayloadType payloadType) {
        // Authenticate specific trader account
        return connection.closePosition(clientMsgId, positionId, volume, payloadType);
    }

    public CompletableFuture<String> authenticateTraderAccount(
            CTraderConnection connection, int ctidTraderAccountId) {
        // Authenticate specific trader account
        return connection.authenticateTraderAccount(ctidTraderAccountId);
    }

    public String getAccessToken(String clientId, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of(
                "grant_type", "client_credentials",
                "client_id", clientId,
                "client_secret", clientSecret
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                CTRADER_API_BASE_URL,
                request,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    public String refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                CTRADER_API_BASE_URL,
                request,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    public List<Map<String, Object>> getAccounts(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(CTRADER_API_BASE_URL, HttpMethod.GET, request, Map.class);


        return (List<Map<String, Object>>) response.getBody().get("accounts");
    }

    public String getAccessToken(Long accountId) {
        // Trong thực tế, có thể gọi REST API để lấy token nếu cần
        return accountTokens.getOrDefault(accountId, "default-token");
    }

    public Map<String, String> getAllAccountCredentials() {
        // Trả về tất cả thông tin tài khoản (accountId -> accessToken)
        return new HashMap<>(accountTokens);
    }

    // Ví dụ: Gọi REST API để lấy thông tin tài khoản nếu cTrader hỗ trợ
    public String fetchAccountDetails(String accountId) {
        String url = "https://some-ctrader-rest-api/accounts/" + accountId;
        return restTemplate.getForObject(url, String.class);
    }
}