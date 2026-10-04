package com.tradematching.controller;

import com.tradematching.dto.ApiResponse;
import com.tradematching.model.MarketDepth;
import com.tradematching.model.Trade;
import com.tradematching.service.MatchingEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketDataController {

    private final MatchingEngineService matchingEngine;

    public MarketDataController(MatchingEngineService matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @GetMapping("/symbols")
    public ResponseEntity<ApiResponse<Set<String>>> getSymbols() {
        return ResponseEntity.ok(ApiResponse.ok(matchingEngine.getActiveSymbols()));
    }

    @GetMapping("/depth/{symbol}")
    public ResponseEntity<ApiResponse<MarketDepth>> getDepth(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "15") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(matchingEngine.getMarketDepth(symbol, limit)));
    }

    @GetMapping("/trades/{symbol}")
    public ResponseEntity<ApiResponse<List<Trade>>> getRecentTrades(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(matchingEngine.getRecentTrades(symbol, limit)));
    }
}
