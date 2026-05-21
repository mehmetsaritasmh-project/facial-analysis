package com.yuzanalizi.yuzanalizapi.controller;

import com.yuzanalizi.yuzanalizapi.dto.ApiResponse;
import com.yuzanalizi.yuzanalizapi.service.FaceDetectionService;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private FaceDetectionService faceDetectionService;

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("OpenCV kütüphanesi yüklenemedi: " + e.getMessage());
        }
    }

    @PostMapping("/detect")
    public ApiResponse<String> detect(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                // Burada (boolean, mesaj, data) formatını kullanıyoruz
                return new ApiResponse<>(false, "Dosya boş", null);
            }
            byte[] bytes = file.getBytes();
            Mat image = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
            
            if (image.empty()) {
                return new ApiResponse<>(false, "Görsel işlenemedi", null);
            }

            String result = faceDetectionService.detectFace(image);
            return new ApiResponse<>(true, "Başarılı", result);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Hata: " + e.getMessage(), null);
        }
    }
}