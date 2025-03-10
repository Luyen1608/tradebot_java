package luyen.tradebot.Trade.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class CTraderAuthService {
    private static final String TOKEN_URL = "https://oauth2.ctrader.com/oauth/token";
    private static final String CLIENT_ID = "your_client_id";
    private static final String CLIENT_SECRET = "your_client_secret";

    public String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        // Tạo body request
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "client_credentials");
        requestBody.put("client_id", CLIENT_ID);
        requestBody.put("client_secret", CLIENT_SECRET);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Gửi request
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, requestEntity, Map.class);

        if (response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }
        return null;
    }
}
