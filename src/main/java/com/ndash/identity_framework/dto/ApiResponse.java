package com.ndash.identity_framework.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {

    private String statusMessage;
    private int statusCode;
    private T data;
    private String error;

    public ApiResponse(String statusMessage, int statusCode, T data, String error) {
        this.statusMessage = statusMessage;
        this.statusCode = statusCode;
        this.data = data;
        this.error = error;
    }
    public static <T> ApiResponse<T> success(T data, int statusCode) {
        return new ApiResponse<>("SUCCESS", statusCode, data, null);
    }

    public static <T> ApiResponse<T> error(String errorMessage, int statusCode) {
        return new ApiResponse<>("ERROR", statusCode, null, errorMessage);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

