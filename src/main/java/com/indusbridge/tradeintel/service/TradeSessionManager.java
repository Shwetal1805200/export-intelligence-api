package com.indusbridge.tradeintel.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TradeSessionManager {

    private static final String TARGET_URL = "https://tradestat.commerce.gov.in/eidb/commodity_wise_all_countries_export";
    
    // In-memory cache for the session (Valid for ~4 minutes)
    private String currentCsrfToken;
    private String currentCookies;
    private Instant tokenCreatedAt;

    private final HttpClient httpClient;

    public TradeSessionManager() {
        // Create a robust HTTP client that handles redirects automatically
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public synchronized String getValidToken() {
        // If token is missing or older than 4 minutes (240 seconds), refresh it
        if (currentCsrfToken == null || Instant.now().minusSeconds(240).isAfter(tokenCreatedAt)) {
            refreshSession();
        }
        return currentCsrfToken;
    }

    public synchronized String getCookies() {
        if (currentCookies == null) {
            refreshSession();
        }
        return currentCookies;
    }

    private void refreshSession() {
        System.out.println("Refreshing Tradestat Session and CSRF Token...");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TARGET_URL))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)") // Mimic a real browser
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 1. Extract Cookies (XSRF-TOKEN and laravel_session)
            List<String> cookieHeaders = response.headers().allValues("set-cookie");
            this.currentCookies = cookieHeaders.stream()
                    .map(cookie -> cookie.split(";")[0]) // Grab only the key=value part
                    .collect(Collectors.joining("; "));

            // 2. Parse HTML to find the hidden _token field using Jsoup
            Document doc = Jsoup.parse(response.body());
            Element tokenElement = doc.selectFirst("input[name=_token]");

            if (tokenElement != null) {
                this.currentCsrfToken = tokenElement.val();
                this.tokenCreatedAt = Instant.now();
                System.out.println("Session successfully established.");
            } else {
                throw new RuntimeException("Could not find _token in Tradestat HTML.");
            }

        } catch (Exception e) {
            System.err.println("Failed to establish Tradestat session: " + e.getMessage());
            throw new RuntimeException("Session refresh failed", e);
        }
    }
}