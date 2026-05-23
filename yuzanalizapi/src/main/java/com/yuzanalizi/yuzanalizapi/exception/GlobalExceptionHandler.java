package com.yuzanalizi.yuzanalizapi.exception;

import com.yuzanalizi.yuzanalizapi.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 500 - Sunucu Hataları
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleAllExceptions(Exception ex) {
        // Not: Eğer dto içindeki ApiResponse sınıfının yapısı farklıysa (constructor vs.) burası kızarabilir, bana atarsın düzeltiriz.
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Sunucu Hatası: " + ex.getMessage());
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 400 - Kötü İstek
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleBadRequest(IllegalArgumentException ex) {
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Geçersiz İstek: " + ex.getMessage());
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}