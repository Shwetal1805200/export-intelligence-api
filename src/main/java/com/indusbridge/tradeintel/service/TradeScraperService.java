package com.indusbridge.tradeintel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TradeScraperService {

    // Pulls the "10" from application.properties. Defaults to 10 if missing.
    @Value("${indusbridge.scraper.historical-years:10}")
    private int historicalYears;

    private final HttpClient httpClient;

    public TradeScraperService() {
        // A robust client that times out after 15 seconds so your app never freezes
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public List<String> fetchHistoricalData(String hsCode, String token, String cookies) {
        int latestYear = 2025; // Tradestat's latest full available year
        List<CompletableFuture<String>> futures = new ArrayList<>();

        System.out.println("Initiating parallel scrape for " + historicalYears + " years...");

        // Fire off all HTTP POST requests simultaneously
        for (int i = 0; i < historicalYears; i++) {
            int targetYear = latestYear - i;
            futures.add(fetchYearAsync(hsCode, targetYear, token, cookies));
        }

        // Wait for all 10 threads to finish, then collect the raw HTML responses
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private CompletableFuture<String> fetchYearAsync(String hsCode, int year, String token, String cookies) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // The exact form data Tradestat expects
                String formData = "Eidbhscode_cmace=" + hsCode +
                                  "&EidbYear_cmace=" + year +
                                  "&EidbReport_cmace=2" +
                                  "&_token=" + token;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://tradestat.commerce.gov.in/eidb/commodity_wise_all_countries_export"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Cookie", cookies)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36") // Mimic a real browser perfectly
                        .POST(HttpRequest.BodyPublishers.ofString(formData))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                System.out.println("✅ Successfully fetched data for year: " + year);
                return response.body(); // Returns the raw HTML of the table
                
            } catch (Exception e) {
                System.err.println("❌ Failed to fetch year " + year + ": " + e.getMessage());
                return ""; // Return empty string so one failure doesn't crash the whole batch
            }
        });
    }
}