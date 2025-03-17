package luyen.tradebot.Trade.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @PostMapping("/ctrader")
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature) {

        logger.info("Webhook Received: {}", payload);
        logger.info("Signature: {}", signature);

        // Xác thực chữ ký (nếu có)
        if (!validateSignature(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        // Xử lý dữ liệu
        return ResponseEntity.ok("Webhook processed successfully");
    }

    private boolean validateSignature(String payload, String signature) {
        // Giả sử có hàm xác thực chữ ký HMAC-SHA256
        return true;
    }
}