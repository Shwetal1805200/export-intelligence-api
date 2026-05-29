package com.indusbridge.tradeintel.dto;

public class AiInsightDto {
    private String insightText;

    // Default Constructor (Required by Spring)
    public AiInsightDto() {
    }

    // Parameterized Constructor
    public AiInsightDto(String insightText) {
        this.insightText = insightText;
    }

    // Getter
    public String getInsightText() {
        return insightText;
    }

    // Setter
    public void setInsightText(String insightText) {
        this.insightText = insightText;
    }
}