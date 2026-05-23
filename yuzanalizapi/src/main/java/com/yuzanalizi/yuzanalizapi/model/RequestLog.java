package com.yuzanalizi.yuzanalizapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "istek_loglari") // Artık MongoDB koleksiyonu oldu
public class RequestLog {
    
    @Id
    private String id; // MongoDB'de ID genelde String olur
    
    private String islemTipi;
    private long gecenSureMs;

    // --- Getter ve Setter'lar ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    // ... diğerleri aynı

    public String getIslemTipi() { return islemTipi; }
    public void setIslemTipi(String islemTipi) { this.islemTipi = islemTipi; }

    public long getGecenSureMs() { return gecenSureMs; }
    public void setGecenSureMs(long gecenSureMs) { this.gecenSureMs = gecenSureMs; }
}