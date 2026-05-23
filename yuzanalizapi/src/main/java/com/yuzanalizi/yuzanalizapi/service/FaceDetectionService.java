package com.yuzanalizi.yuzanalizapi.service;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;

@Service
public class FaceDetectionService {

    // Arka planda 8000 portunda çalışan Python FastAPI yapay zeka servisimiz
    // Python servisinin gerçek kapı numarası olan /analyze-face uzantısını ekledik!
    private final String PYTHON_SERVICE_URL = "http://127.0.0.1:8000/analyze-face";
    
    public String detectFace(Mat image) {
        try {
            // 1. OpenCV Mat nesnesini byte array'e (görsele) dönüştürüyoruz
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", image, matOfByte);
            byte[] imageBytes = matOfByte.toArray();

            // 2. Python servisine Multipart isteği hazırlıyoruz
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "image.jpg"; // Python tarafının dosyayı tanıması için isim şart
                }
            };
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 3. Python'a isteği atıp gelen ham JSON verisini (olcumler, etiketler) aynen alıyoruz
            String pythonResponse = restTemplate.postForObject(PYTHON_SERVICE_URL, requestEntity, String.class);
            
            return pythonResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Python servisine bağlanılamadı: " + e.getMessage() + "\"}";
        }
    }
}