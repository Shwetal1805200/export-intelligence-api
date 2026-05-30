package com.indusbridge.tradeintel.dto;

import java.util.List;

public class TradeResponseDto {
    private String hsCode;
    private String commodityName;
    private List<CountryDataDto> topCountries;
    private HistoricalTrendDto historicalTrend; // NEW FIELD

    public TradeResponseDto() {}

    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }

    public String getCommodityName() { return commodityName; }
    public void setCommodityName(String commodityName) { this.commodityName = commodityName; }

    public List<CountryDataDto> getTopCountries() { return topCountries; }
    public void setTopCountries(List<CountryDataDto> topCountries) { this.topCountries = topCountries; }

    public HistoricalTrendDto getHistoricalTrend() { return historicalTrend; }
    public void setHistoricalTrend(HistoricalTrendDto historicalTrend) { this.historicalTrend = historicalTrend; }
}