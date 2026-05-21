package com.yuzanalizi.yuzanalizapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuzanalizi.yuzanalizapi.dto.AnalysisResponse;
import com.yuzanalizi.yuzanalizapi.dto.ApiResponse;
import com.yuzanalizi.yuzanalizapi.model.FaceAnalysisResult;
import com.yuzanalizi.yuzanalizapi.model.RequestLog;
import com.yuzanalizi.yuzanalizapi.repository.FaceAnalysisMongoRepository;
import com.yuzanalizi.yuzanalizapi.repository.LogJdbcRepository;
import com.yuzanalizi.yuzanalizapi.service.PythonBridgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final PythonBridgeService pythonBridgeService;
    
    // Veritabanı arayüzlerini ve JSON dönüştürücüyü sisteme dahil ediyoruz (Dependency Injection)
    private final LogJdbcRepository logJdbcRepository;
    private final FaceAnalysisMongoRepository mongoRepository;
    private final ObjectMapper objectMapper;

    public AnalysisController(PythonBridgeService pythonBridgeService,
                              LogJdbcRepository logJdbcRepository,
                              FaceAnalysisMongoRepository mongoRepository,
                              ObjectMapper objectMapper) {
        this.pythonBridgeService = pythonBridgeService;
        this.logJdbcRepository = logJdbcRepository;
        this.mongoRepository = mongoRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/full-analysis")
    public ResponseEntity<ApiResponse<AnalysisResponse>> analyze(@RequestParam("file") MultipartFile file) throws Exception {
        
        long startTime = System.currentTimeMillis();

        // 1. Android/Flutter'dan gelen resmi Python'a aktar ve sonucu al
        AnalysisResponse response = pythonBridgeService.sendToPythonForAnalysis(file);

        // 2. MONGODB KAYDI (NoSQL İsteri - 10 Puan)
        // Python'dan dönen veriyi JSON formatına çevirip belgesel olarak kaydediyoruz.
        FaceAnalysisResult mongoResult = new FaceAnalysisResult();
        mongoResult.setRawJsonData(objectMapper.writeValueAsString(response)); 
        mongoResult.setTimestamp(System.currentTimeMillis());
        mongoRepository.save(mongoResult);

        // 3. JDBC/H2 KAYDI (SQL/İlişkisel Veritabanı İsteri - 10 Puan)
        // İşlemin ne kadar sürdüğünü ve tipini logluyoruz.
        long duration = System.currentTimeMillis() - startTime;
        RequestLog log = new RequestLog();
        log.setIslemTipi("Tam_Yuz_Analizi");
        log.setGecenSureMs(duration);
        logJdbcRepository.save(log);

        // 4. GENERIC YAPILAR (Tip Güvenliği İsteri - 10 Puan)
        // Dönüş tipini doğrudan vermek yerine ApiResponse<T> içine sarmalıyoruz.
        ApiResponse<AnalysisResponse> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Analiz başarıyla tamamlandı ve veritabanlarına kaydedildi.");
        apiResponse.setData(response);

        return ResponseEntity.ok(apiResponse);
    }
}