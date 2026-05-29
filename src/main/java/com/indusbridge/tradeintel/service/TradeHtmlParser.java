package com.indusbridge.tradeintel.service;

import com.indusbridge.tradeintel.dto.CountryDataDto;
import com.indusbridge.tradeintel.dto.TradeResponseDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TradeHtmlParser {

    public TradeResponseDto parseData(String hsCode, List<String> rawHtmlPages) {
        TradeResponseDto response = new TradeResponseDto();
        response.setHsCode(hsCode);
        List<CountryDataDto> countryDataList = new ArrayList<>();

        if (rawHtmlPages == null || rawHtmlPages.isEmpty()) {
            return response;
        }

        String mostRecentHtml = rawHtmlPages.get(0); 
        
        try {
            Document doc = Jsoup.parse(mostRecentHtml);
            
            // Attempt to grab the title
            Element titleElement = doc.selectFirst("font[color=black]"); 
            if (titleElement != null) {
                response.setCommodityName(titleElement.text());
            } else {
                response.setCommodityName("Commodity Data");
            }

            // Grab all rows in the table
            Elements rows = doc.select("table tr");
            
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                
                // Tradestat uses <td> for data. If it's a <th> header row, cols.size() will be 0.
                Elements cols = row.select("td");

                // We need at least 8 columns based on the image (S.No, Country, Val1, Val2, Val3, Qty1, Qty2, Qty3)
                if (cols.size() >= 8) {
                    String countryName = cols.get(1).text().trim();
                    
                    // CRITICAL TRAP AVOIDANCE: Stop parsing when we hit the summary rows at the bottom
                    if (countryName.equalsIgnoreCase("Total") || 
                        countryName.equalsIgnoreCase("India's Total") || 
                        countryName.equalsIgnoreCase("%Share")) {
                        break; 
                    }

                    CountryDataDto country = new CountryDataDto();
                    country.setCountryName(countryName);
                    
                    try {
                        // Values in US $ Million (Indices 2, 3, 4)
                        country.setValuePreviousYear(parseSafeDouble(cols.get(2).text()));
                        country.setValueCurrentYear(parseSafeDouble(cols.get(3).text()));
                        country.setValueGrowthPercent(parseSafeDouble(cols.get(4).text()));
                        
                        // Values in Quantity (Indices 5, 6, 7)
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