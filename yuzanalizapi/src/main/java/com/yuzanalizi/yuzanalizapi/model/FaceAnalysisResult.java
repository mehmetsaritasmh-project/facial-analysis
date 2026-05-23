package com.yuzanalizi.yuzanalizapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "analiz_sonuclari")
public class FaceAnalysisResult {
    
    @Id
    private String id;
    private String rawJsonData; // Python'dan gelen analiz verisi
    private long timestamp;

    // --- Getter ve Setter'lar ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRawJsonData() { return rawJsonData; }
    public void setRawJsonData(String rawJsonData) { this.rawJsonData = rawJsonData; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}