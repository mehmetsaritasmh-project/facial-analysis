package com.yuzanalizi.yuzanalizapi.dto;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    // Boş Constructor (Zorunlu)
    public ApiResponse() {}

    // Dolu Constructor
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getter & Setterlar (Lombok'a ihtiyaç kalmadı)
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}