package luyen.tradebot.Trade.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.util.enumTraderBot.AccountType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CTraderApiService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ctrader.ws.demo.url}")
    private String CTRADER_API_BASE_URL;

    @Value("${ctrader.ws.demo.url}")
    private String CTRADER_DEMO_WS_URL;

    @Value("${ctrader.ws.live.url}")
    private String CTRADER_LIVE_WS_URL;

    public CTraderConnection connect(String clientId, String clientSecret, String accessToken, AccountType accountType) {
        // Implement WebSocket connection to cTrader API
        String wsUrl = "DEMO".equalsIgnoreCase(accountType.toString()) ?
                CTRADER_DEMO_WS_URL : CTRADER_LIVE_WS_URL;

        CTraderConnection connection = new CTraderConnection(clientId, accessToken, wsUrl);
        connection.connect();
        return connection;
    }
    public CompletableFuture<List<Map<String, Object>>> getTraderAccounts(CTraderConnection connection) {
        // Get trader accounts via WebSocket
        return connection.getAccountListByAccessToken();
    }

    public CompletableFuture<Boolean> authenticateTraderAccount(
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

        ResponseEntity<Map> response = restTemplate.exchange(CTRADER_API_BASE_URL, HttpMethod.GET,request,Map.class);


        return (List<Map<String, Object>>) response.getBody().get("accounts");
    }
}