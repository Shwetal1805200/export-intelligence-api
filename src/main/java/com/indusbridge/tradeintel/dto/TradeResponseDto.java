package com.indusbridge.tradeintel.dto;

import java.util.List;

public class TradeResponseDto {
    private String hsCode;
    private String commodityName;
    private List<CountryDataDto> topCountries;

    // Default Constructor
    public TradeResponseDto() {
    }

    // Getters
    public String getHsCode() {
        return hsCode;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public List<CountryDataDto> getTopCountries() {
        return topCountries;
    }

    // Setters
    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    public void setTopCountries(List<CountryDataDto> topCountries) {
        this.topCountries = topCountries;
    }
}