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
    @Cacheable(value = "trade_insights_real", key = "#tradeData.hsCode + '-' + #userQuestion")
    public String analyzeRealData(TradeResponseDto tradeData, String userQuestion) {
        System.out.println(">>> AI ENGINE: Analyzing real market data for HS Code " + tradeData.getHsCode());
        
        try {
            // Convert your giant Java object back into a tight JSON string so Gemini can read it
            String rawJsonData = objectMapper.writeValueAsString(tradeData.getTopCountries());
            
            // Build the engineered prompt
            String engineeredPrompt = "You are an expert global trade analyst for IndusBridge Global. " +
                    "Analyze the following real export data for HS Code " + tradeData.getHsCode() + ". " +
                    "The data is a JSON array of countries, showing previous year value, current year value, and growth percentage. " +
                    "Here is the data: " + rawJsonData + ". " +
                    "Based strictly on this data, answer the following question clearly and professionally: " + userQuestion;

            return callGemini(engineeredPrompt, ""); // Send to the helper method below
            
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