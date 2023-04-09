package com.serverless.model;

public class CommonAPIResponse {
    private String message;

    public CommonAPIResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
