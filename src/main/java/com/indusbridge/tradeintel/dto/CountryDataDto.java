package com.indusbridge.tradeintel.dto;

public class CountryDataDto {
    private String countryName;
    
    // Value Data (US $ Million)
    private double valuePreviousYear;
    private double valueCurrentYear;
    private double valueGrowthPercent;
    
    // Quantity Data
    private double qtyPreviousYear;
    private double qtyCurrentYear;
    private double qtyGrowthPercent;

    public CountryDataDto() {}

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public double getValuePreviousYear() { return valuePreviousYear; }
    public void setValuePreviousYear(double valuePreviousYear) { this.valuePreviousYear = valuePreviousYear; }

    public double getValueCurrentYear() { return valueCurrentYear; }
    public void setValueCurrentYear(double valueCurrentYear) { this.valueCurrentYear = valueCurrentYear; }

    public double getValueGrowthPercent() { return valueGrowthPercent; }
    public void setValueGrowthPercent(double valueGrowthPercent) { this.valueGrowthPercent = valueGrowthPercent; }

    public double getQtyPreviousYear() { return qtyPreviousYear; }
    public void setQtyPreviousYear(double qtyPreviousYear) { this.qtyPreviousYear = qtyPreviousYear; }

    public double getQtyCurrentYear() { return qtyCurrentYear; }
    public void setQtyCurrentYear(double qtyCurrentYear) { this.qtyCurrentYear = qtyCurrentYear; }

    public double getQtyGrowthPercent() { return qtyGrowthPercent; }
    public void setQtyGrowthPercent(double qtyGrowthPercent) { this.qtyGrowthPercent = qtyGrowthPercent; }
}