package com.yuzanalizi.yuzanalizapi.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuzanalizi.yuzanalizapi.dto.AnalysisResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PythonBridgeService {

    private final RestTemplate restTemplate;
    private final String PYTHON_URL = "http://localhost:8000/analyze-face";

    public PythonBridgeService() {
        // RestTemplate'in sonsuza kadar kilitlenmesini engellemek için süre sınırı koyuyoruz
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // Bağlanma süresi: 30 saniye
        factory.setReadTimeout(30000);    // Python'dan cevap bekleme süresi: 30 saniye
        this.restTemplate = new RestTemplate(factory);
    }

    public AnalysisResponse sendToPythonForAnalysis(MultipartFile file) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // 1. Python'dan veriyi kilitlenme riskine karşı ham String olarak çekiyoruz
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PYTHON_URL, requestEntity, String.class);
            String rawJson = responseEntity.getBody();

            // 2. Terminalde neyin uyuşmadığını görebilmek için ham veriyi buraya basıyoruz
            System.out.println("\n=======================================");
            System.out.println("====== PYTHON'DAN GELEN HAM VERİ ======");
            System.out.println(rawJson);
            System.out.println("=======================================\n");

            // 3. Güvenli dönüştürücü (ObjectMapper) kurulumu
            ObjectMapper mapper = new ObjectMapper();
            // Eşleşmeyen veya DTO'da eksik olan alanlar varsa Java'nın tıkanmasını/çökmesini engeller:
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // String formatındaki JSON'ı DTO sınıfına güvenli bir şekilde map'liyoruz
            return mapper.readValue(rawJson, AnalysisResponse.class);

        } catch (Exception e) {
            System.err.println("🚨 PYTHON KÖPRÜSÜNDE HATA: " + e.getMessage());
            throw new RuntimeException("Python analiz servisi işleme esnasında hata döndürdü veya süre aşımına uğradı: " + e.getMessage());
        }
    }
}