package luyen.tradebot.Trade.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.MessageTradingViewDTO;
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

    public ResponseEntity<String> processOrder(@Valid @RequestBody MessageTradingViewDTO webhookDTO) {
//        log.info("Received order webhook: {}", webhookDTO);

        //kiểm tra getAction là ENTER_LONG hoặc ENTER_SHORT thì chạy vào hàm processWebhookclose
        log.info("Received order webhook: {}", webhookDTO);
        if ("ENTER_LONG".equalsIgnoreCase(webhookDTO.getAction()) || "ENTER_SHORT".equalsIgnoreCase(webhookDTO.getAction())) {

            orderService.processWebhookOrder(webhookDTO);
            return ResponseEntity.ok("Order placed successfully");
        } else {
            orderService.processWebhookClose(webhookDTO);
            return ResponseEntity.ok("Close position request processed");
        }
    }
}