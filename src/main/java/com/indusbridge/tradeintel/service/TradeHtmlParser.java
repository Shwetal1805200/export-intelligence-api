package com.indusbridge.tradeintel.service;

import com.indusbridge.tradeintel.dto.CountryDataDto;
import com.indusbridge.tradeintel.dto.HistoricalTrendDto;
import com.indusbridge.tradeintel.dto.TradeResponseDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class TradeHtmlParser {

    public TradeResponseDto parseData(String hsCode, List<String> rawHtmlPages) {
        TradeResponseDto response = new TradeResponseDto();
        response.setHsCode(hsCode);
        List<CountryDataDto> countryDataList = new ArrayList<>();

        if (rawHtmlPages == null || rawHtmlPages.isEmpty()) {
            return response;
        }

        // ---------------------------------------------------------
        // HISTORICAL TREND AGGREGATION (Scans ALL pages for the Total row)
        // ---------------------------------------------------------
        Map<String, Double> historicalValues = new TreeMap<>();
        Map<String, Double> historicalQtys = new TreeMap<>();

        for (String html : rawHtmlPages) {
            if (html == null || html.isEmpty()) continue;
            try {
                Document tempDoc = Jsoup.parse(html);
                
                // 1. Find the specific year labels dynamically from the table headers
                Elements headers = tempDoc.select("thead tr th");
                String prevYearLabel = "";
                String currYearLabel = "";
                
                for (Element th : headers) {
                    String txt = th.text().trim();
                    if (txt.matches("\\d{4}-\\d{4}.*")) { // Matches "2024-2025"
                        if (prevYearLabel.isEmpty()) {
                            prevYearLabel = txt;
                        } else if (currYearLabel.isEmpty()) {
                            currYearLabel = txt;
                        }
                    }
                }

                // 2. Find the "Total" row to grab global aggregated data for those two years
                Elements rows = tempDoc.select("table tr");
                for (Element row : rows) {
                    Elements cols = row.select("td");
                    
                    // The "Total" row uses colspan="2", making it the very first column (index 0)
                    if (!cols.isEmpty() && cols.get(0).text().trim().equalsIgnoreCase("Total")) {
                        
                        if (!prevYearLabel.isEmpty()) {
                            // Because of colspan, Prev Year Value is now at index 1 (not 2)
                            historicalValues.put(prevYearLabel, parseSafeDouble(cols.get(1).text()));
                            historicalQtys.put(prevYearLabel, parseSafeDouble(cols.get(4).text()));
                        }
                        if (!currYearLabel.isEmpty()) {
                            // Curr Year Value is at index 2 (not 3)
                            historicalValues.put(currYearLabel, parseSafeDouble(cols.get(2).text()));
                            historicalQtys.put(currYearLabel, parseSafeDouble(cols.get(5).text()));
                        }
                        break; // Stop scanning rows once we find Total
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to parse historical page chunk.");
            }
        }

        // 3. Package the 5 most recent years into the DTO
        HistoricalTrendDto trend = new HistoricalTrendDto();
        List<String> allYears = new ArrayList<>(historicalValues.keySet());
//        int startIndex = Math.max(0, allYears.size() - 5); // Grab only the last 5 years
        int startIndex = 0;
        
        for (int i = startIndex; i < allYears.size(); i++) {
            String year = allYears.get(i);
            trend.getLabels().add(year);
            trend.getValue().add(historicalValues.get(year));
            trend.getQuantity().add(historicalQtys.get(year));
        }
        response.setHistoricalTrend(trend);


        // ---------------------------------------------------------
        // CURRENT YEAR COUNTRY PARSING (Uses ONLY the most recent page)
        // ---------------------------------------------------------
        String mostRecentHtml = rawHtmlPages.get(0); 
        
        try {
            Document doc = Jsoup.parse(mostRecentHtml);
            
            // PIXEL-PERFECT COMMODITY EXTRACTION
            String extractedName = "Commodity Data"; 
            Elements paragraphs = doc.select("p"); 
            
            for (Element p : paragraphs) {
                String text = p.text().trim(); 
                if (text.toUpperCase().startsWith("COMMODITY")) {
                    extractedName = text.replaceAll("(?i)Commodity\\s*:\\s*", "") 
                                        .split("(?i)Unit")[0]                      
                                        .split("(?i)Export")[0]                    
                                        .replaceAll("[-:;]+$", "")                 
                                        .trim();                                   
                    break; 
                }
            }
            response.setCommodityName(extractedName);

            // DETAILED COUNTRY EXTRACTION
            Elements rows = doc.select("table tr");
            
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cols = row.select("td");

                // Standard country rows have 8 columns
                if (cols.size() >= 8) {
                    String countryName = cols.get(1).text().trim();
                    
                    if (countryName.equalsIgnoreCase("Total") || 
                        countryName.equalsIgnoreCase("India's Total") || 
                        countryName.equalsIgnoreCase("%Share")) {
                        break; 
                    }

                    CountryDataDto country = new CountryDataDto();
                    country.setCountryName(countryName);
                    
                    try {
                        country.setValuePreviousYear(parseSafeDouble(cols.get(2).text()));
                        country.setValueCurrentYear(parseSafeDouble(cols.get(3).text()));
                        country.setValueGrowthPercent(parseSafeDouble(cols.get(4).text()));
                        
                        country.setQtyPreviousYear(parseSafeDouble(cols.get(5).text()));
                        country.setQtyCurrentYear(parseSafeDouble(cols.get(6).text()));
                        country.setQtyGrowthPercent(parseSafeDouble(cols.get(7).text()));
                        
                    } catch (Exception e) {
                        System.err.println("Skipping malformed row for country: " + countryName);
                        continue; 
                    }
                    
                    countryDataList.add(country);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse HTML: " + e.getMessage());
        }

        response.setTopCountries(countryDataList);
        return response;
    }

    private double parseSafeDouble(String text) {
        if (text == null || text.trim().isEmpty() || text.trim().equals("-") || text.trim().equalsIgnoreCase("NA")) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.replaceAll(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.0; 
        }
    }
}