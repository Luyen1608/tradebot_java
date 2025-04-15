package luyen.tradebot.Trade.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luyen.tradebot.Trade.dto.MessageTradingViewDTO;
import luyen.tradebot.Trade.dto.respone.ResponseData;
import luyen.tradebot.Trade.model.AlertTradingEntity;
import luyen.tradebot.Trade.service.AlertTradingService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final AlertTradingService alertTradingService;
    @Operation(summary = "get list alert Trading view by sort page and search")
    @GetMapping("/msg_tradingview")
    public ResponseEntity<Page<AlertTradingEntity>> getListMsgTradingView(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sorts,
            @RequestParam(required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate timestamp) {
        System.out.println("Request get list alert Trading view with sort by multiple column");
        if(timestamp != null){
            LocalDateTime date = timestamp.atStartOfDay();
            Page<AlertTradingEntity> page = alertTradingService.getAlertTradings(pageNo, pageSize, search, sorts, date);
            return ResponseEntity.ok(page);
        } else {
            Page<AlertTradingEntity> page = alertTradingService.getAlertTradings(pageNo, pageSize, search, sorts, null);
            return ResponseEntity.ok(page);
        }

    }

}
