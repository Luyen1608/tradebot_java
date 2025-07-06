package luyen.tradebot.Trade.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.MessageTradingViewDTO;
import luyen.tradebot.Trade.dto.respone.ApiResponse;
import luyen.tradebot.Trade.dto.respone.ResponseData;
import luyen.tradebot.Trade.model.AccountEntity;
import luyen.tradebot.Trade.model.AlertTradingEntity;
import luyen.tradebot.Trade.service.AlertTradingService;
import luyen.tradebot.Trade.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final AlertTradingService alertTradingService;
    private final OrderService orderService;

    @Operation(summary = "get list alert Trading view by sort page and search")
    @GetMapping("/msg_tradingview")
    public ResponseEntity<Page<AlertTradingEntity>> getListMsgTradingView(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sorts,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate timestamp) {
        System.out.println("Request get list alert Trading view with sort by multiple column");
        if (timestamp != null) {
            LocalDateTime date = timestamp.atStartOfDay();
            Page<AlertTradingEntity> page = alertTradingService.getAlertTradings(pageNo, pageSize, search, sorts, date);
            return ResponseEntity.ok(page);
        } else {
            Page<AlertTradingEntity> page = alertTradingService.getAlertTradings(pageNo, pageSize, search, sorts, null);
            return ResponseEntity.ok(page);
        }

    }


    @GetMapping("/orderList")
    public DeferredResult<ResponseEntity<String>> getOrderList(
            @RequestParam UUID accountId,
            @RequestParam Long fromTimestamp,
            @RequestParam Long toTimestamp) {
        
        // Tạo DeferredResult với timeout 120 giây (120000ms)
        DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>(120000L);
        
        // Xử lý logic với fromTimestamp
        if (fromTimestamp == null) {
            fromTimestamp = 0L; // Giá trị mặc định là 1/1/1970
        }
        // Kiểm tra fromTimestamp >= 0
        if (fromTimestamp < 0) {
            deferredResult.setResult(ResponseEntity.badRequest().body("fromTimestamp must be >= 0"));
            return deferredResult;
        }
        // Xử lý logic với toTimestamp
        if (toTimestamp == null) {
            toTimestamp = 0L; // Giá trị mặc định là 1/1/1970
        }

        // Kiểm tra toTimestamp >= 0
        if (toTimestamp < 0) {
            deferredResult.setResult(ResponseEntity.badRequest().body("toTimestamp must be >= 0"));
            return deferredResult;
        }

        // Xử lý timeout
        deferredResult.onTimeout(() -> {
            log.warn("Request timeout for getOrderList with accountId: {}", accountId);
            deferredResult.setResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body("Request timeout after 120 seconds"));
        });

        // Xử lý async
        orderService.getOrderList(accountId, fromTimestamp, toTimestamp)
                .thenAccept(result -> deferredResult.setResult(ResponseEntity.ok(result)))
                .exceptionally(ex -> {
                    log.error("Error in getOrderList: ", ex);
                    deferredResult.setResult(ResponseEntity.status(500).body("⚠ Error: " + ex.getMessage()));
                    return null;
                });

        return deferredResult;
    }
    @GetMapping("/detailList")
    public DeferredResult<ResponseEntity<String>> getDetailList(
            @RequestParam UUID accountId,
            @RequestParam Long fromTimestamp,
            @RequestParam Long toTimestamp,
            @RequestParam(defaultValue = "10") int maxRows) {
        
        // Tạo DeferredResult với timeout 120 giây (120000ms)
        DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>(120000L);
        
        // Xử lý logic với fromTimestamp
        if (fromTimestamp == null) {
            fromTimestamp = 0L; // Giá trị mặc định là 1/1/1970
        }
        // Kiểm tra fromTimestamp >= 0
        if (fromTimestamp < 0) {
            deferredResult.setResult(ResponseEntity.badRequest().body("fromTimestamp must be >= 0"));
            return deferredResult;
        }
        // Xử lý logic với toTimestamp
        if (toTimestamp == null) {
            toTimestamp = 0L; // Giá trị mặc định là 1/1/1970
        }

        // Kiểm tra toTimestamp >= 0
        if (toTimestamp < 0) {
            deferredResult.setResult(ResponseEntity.badRequest().body("toTimestamp must be >= 0"));
            return deferredResult;
        }

        // Xử lý timeout
        deferredResult.onTimeout(() -> {
            log.warn("Request timeout for getDetailList with accountId: {}", accountId);
            deferredResult.setResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body("Request timeout after 120 seconds"));
        });

        // Xử lý async
        orderService.getDetailList(accountId, fromTimestamp, toTimestamp, maxRows)
                .thenAccept(result -> deferredResult.setResult(ResponseEntity.ok(result)))
                .exceptionally(ex -> {
                    log.error("Error in getDetailList: ", ex);
                    deferredResult.setResult(ResponseEntity.status(500).body("⚠ Error: " + ex.getMessage()));
                    return null;
                });

        return deferredResult;
    }
}
