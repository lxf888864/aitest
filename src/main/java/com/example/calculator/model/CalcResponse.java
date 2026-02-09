package com.example.calculator.model;

public class CalcResponse {
    private double result;
    private String message;

    public CalcResponse(double result, String message) {
        this.result = result;
        this.message = message;
    }

    public double getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}
