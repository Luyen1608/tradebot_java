package luyen.tradebot.Trade.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.OrderWebhookDTO;
import luyen.tradebot.Trade.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    private final OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<String> processOrder(@RequestBody OrderWebhookDTO webhookDTO) {
        log.info("Received order webhook: {}", webhookDTO);

        if ("close".equalsIgnoreCase(webhookDTO.getType())) {
            orderService.processWebhookClose(webhookDTO);
            return ResponseEntity.ok("Close position request processed");
        } else {
            orderService.processWebhookOrder(webhookDTO);
            return ResponseEntity.ok("Order placed successfully");
        }
    }
}