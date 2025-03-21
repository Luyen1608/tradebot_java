package luyen.tradebot.Trade.controller.tradeBot;


import lombok.RequiredArgsConstructor;
import luyen.tradebot.Trade.dto.request.OrderDTO;
import luyen.tradebot.Trade.model.OrderEntity;
import luyen.tradebot.Trade.model.OrderPosition;
import luyen.tradebot.Trade.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderEntity> placeOrder(@RequestBody OrderDTO orderDTO) {
        OrderEntity order = orderService.placeOrder(orderDTO);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/{orderId}/account/{accountId}/close")
    public ResponseEntity<OrderEntity> closeOrder(@PathVariable Long orderId, @PathVariable Long accountId) {
        OrderEntity order = orderService.closeOrder(orderId, accountId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<OrderEntity>> getOrdersByAccountId(@PathVariable Long accountId) {
        List<OrderEntity> orders = orderService.getOrdersByAccountId(accountId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderEntity>> getOrdersByStatus(@PathVariable String status) {
        List<OrderEntity> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
    @GetMapping("/{orderId}/positions")
    public ResponseEntity<List<OrderPosition>> getPositionsByOrderId(@PathVariable Long orderId) {
        List<OrderPosition> positions = orderService.getPositionsByOrderId(orderId);
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/positions/account/{accountId}")
    public ResponseEntity<List<OrderPosition>> getPositionsByAccountId(@PathVariable Long accountId) {
        List<OrderPosition> positions = orderService.getPositionsByAccountId(accountId);
        return ResponseEntity.ok(positions);
    }
}