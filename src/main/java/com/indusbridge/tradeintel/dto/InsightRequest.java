package com.indusbridge.tradeintel.dto;

public class InsightRequest {
    private String hsCode;
    private String question;

    // Default Constructor
    public InsightRequest() {
    }

    // Getters
    public String getHsCode() {
        return hsCode;
    }

    public String getQuestion() {
        return question;
    }

    // Setters
    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}