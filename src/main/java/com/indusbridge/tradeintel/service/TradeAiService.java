package com.indusbridge.tradeintel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indusbridge.tradeintel.dto.TradeResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TradeAiService {

    @Value("${gemini.api.url}")
    private String apiUrl;
    @Value("${gemini.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Keep your old method just in case we need it for testing
    @Cacheable(value = "trade_insights_mock", key = "#hsCode + '-' + #userQuestion")
    public String generateTradeInsight(String hsCode, String userQuestion) {
        String contextData = "Mock Data: Exports for HS Code " + hsCode + " grew by 12%.";
        return callGemini(contextData, userQuestion);
    }

    // THIS IS THE NEW PRODUCTION METHOD
 // THIS IS THE NEW PRODUCTION METHOD
    @Cacheable(value = "trade_insights_real", key = "#tradeData.hsCode + '-' + #userQuestion")
    public String analyzeRealData(TradeResponseDto tradeData, String userQuestion) {
        System.out.println(">>> AI ENGINE: Analyzing real market data for HS Code " + tradeData.getHsCode());
        
        try {
            // 1. Grab the 2-Year Country Data
            String rawCountryData = objectMapper.writeValueAsString(tradeData.getTopCountries());
            
            // 2. Grab the 10-Year Historical Trend Data
            String rawHistoricalData = "No history available";
            if (tradeData.getHistoricalTrend() != null) {
                rawHistoricalData = objectMapper.writeValueAsString(tradeData.getHistoricalTrend());
            }
            
 
         // 3. Build the engineered prompt with Advanced AI Directives
            String engineeredPrompt = 
                "You are an elite Global Trade Analyst and Intelligence Copilot for IndusBridge Global. " +
                "You are analyzing export data for HS Code " + tradeData.getHsCode() + ". " +
                "Dataset 1 (10-Year Macro Trend): " + rawHistoricalData + ". " +
                "Dataset 2 (Country Specific Performance, Prev vs Curr Year): " + rawCountryData + ". " +
                
                "CRITICAL DIRECTIVES: " +
                "1. DO THE MATH: Never just repeat raw numbers. Calculate momentum, identify hidden anomalies, and spot concentration risks. " +
                "2. SYNTHESIZE & DEDUCE: If the user asks a broad question, combine the 10-year macro trend with the 2-year country momentum to deduce the most strategic answer. " +
                "3. FIND THE INVISIBLE: Point out if a country is buying at a historic discount, or if a macro trend signals an upcoming market shift. " +
                "4. FORMATTING & TONE (CRITICAL): Keep answers brutally short, punchy, and highly informative. Absolutely no fluff, conversational filler, or long introductory/concluding paragraphs. Get straight to the point. Use bullet points and bold text for quick scannability. Deliver maximum intelligence in the fewest words possible. " +
                
                "User Query: " + userQuestion;

            return callGemini(engineeredPrompt, ""); // Send to the helper method
            
        } catch (Exception e) {
            System.err.println("Failed to parse data for AI: " + e.getMessage());
            return "Intelligence Engine offline. Data parsing failed.";
        }
    }

    // Helper method to actually make the HTTP call to Google
    private String callGemini(String promptText, String userQuestion) {
        try {
            String fullPrompt = promptText + " " + userQuestion;
            
            // Sanitize quotes so the JSON doesn't break
            String sanitizedPrompt = fullPrompt.replace("\"", "\\\"").replace("\n", " ");

            String requestBody = """
                {
                  "contents": [{"parts":[{"text": "%s"}]}]
                }
                """.formatted(sanitizedPrompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            String responseJson = restTemplate.postForObject(apiUrl + apiKey, request, String.class);
            JsonNode root = objectMapper.readTree(responseJson);
            
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
        } catch (Exception e) {
            System.err.println("Gemini API Error: " + e.getMessage());
            return "Intelligence Engine offline. Cannot connect to Gemini.";
        }
    }
}