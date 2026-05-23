package com.yuzanalizi.yuzanalizapi.dto;

import java.util.Map;

public class AnalysisResponse {
    private Map<String, Object> olcumler;
    private Map<String, String> etiketler;

    // Getters & Setters
    public Map<String, Object> getOlcumler() { return olcumler; }
    public void setOlcumler(Map<String, Object> olcumler) { this.olcumler = olcumler; }

    public Map<String, String> getEtiketler() { return etiketler; }
    public void setEtiketler(Map<String, String> etiketler) { this.etiketler = etiketler; }
}