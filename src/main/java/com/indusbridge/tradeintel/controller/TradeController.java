package com.indusbridge.tradeintel.controller;

import com.indusbridge.tradeintel.dto.AiInsightDto;
import com.indusbridge.tradeintel.dto.InsightRequest;
import com.indusbridge.tradeintel.dto.TradeResponseDto;
import com.indusbridge.tradeintel.service.TradeAiService;
import com.indusbridge.tradeintel.service.TradeHtmlParser;
import com.indusbridge.tradeintel.service.TradeScraperService;
import com.indusbridge.tradeintel.service.TradeSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trade")
@CrossOrigin(origins = "*")
public class TradeController {

    @Autowired
    private TradeSessionManager sessionManager;

    @Autowired
    private TradeScraperService scraperService;

    @Autowired
    private TradeAiService aiService;

    @Autowired
    private TradeHtmlParser htmlParser;

    @GetMapping("/search")
    public ResponseEntity<TradeResponseDto> searchTradeData(@RequestParam String hsCode) {
        System.out.println(">>> ENDPOINT HIT: Received request for HS Code: " + hsCode);
        
        String sessionToken = sessionManager.getValidToken();
        String cookies = sessionManager.getCookies();
        
        List<String> rawHtmlPages = scraperService.fetchHistoricalData(hsCode, sessionToken, cookies);
        TradeResponseDto finalData = htmlParser.parseData(hsCode, rawHtmlPages);

        return ResponseEntity.ok(finalData);
    }
 

    @PostMapping("/insights")
    public ResponseEntity<AiInsightDto> getInsights(@RequestBody InsightRequest request) {
        
        System.out.println(">>> ENDPOINT HIT: Generating AI Insight for HS Code " + request.getHsCode());
        
        // 1. Scrape the data first (we need the numbers to give to the AI!)
        String sessionToken = sessionManager.getValidToken();
        String cookies = sessionManager.getCookies();
        List<String> rawHtmlPages = scraperService.fetchHistoricalData(request.getHsCode(), sessionToken, cookies);
        
        // 2. Parse the HTML into our TradeResponseDto
        TradeResponseDto realMarketData = htmlParser.parseData(request.getHsCode(), rawHtmlPages);

        // 3. Hand the real data + the user's question to Gemini
        String aiResponseText = aiService.analyzeRealData(realMarketData, request.getQuestion());
        
        // 4. Return the AI's brilliant answer
        return ResponseEntity.ok(new AiInsightDto(aiResponseText));
    }
}