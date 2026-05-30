package com.indusbridge.tradeintel.dto;

import java.util.ArrayList;
import java.util.List;

public class HistoricalTrendDto {
    private List<String> labels = new ArrayList<>();
    private List<Double> value = new ArrayList<>();
    private List<Double> quantity = new ArrayList<>();

    public HistoricalTrendDto() {}

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<Double> getValue() { return value; }
    public void setValue(List<Double> value) { this.value = value; }

    public List<Double> getQuantity() { return quantity; }
    public void setQuantity(List<Double> quantity) { this.quantity = quantity; }
}