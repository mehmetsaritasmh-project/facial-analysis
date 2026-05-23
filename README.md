# Yüz Analizi Ve Fizyonomi

Kocaeli Üniversitesi Teknoloji Fakültesi Bilişim Sistemleri Mühendisliği  
**TBL324 - İleri Java Uygulamaları Dersi Proje Raporu** 
**Hazırlayan:** Mehmet Sarıtaş (191307072)  


---

## 🏛️ 1. Proje Genel Mimarisi ve Servis Orkestrasyonu

Bu proje, yüksek performanslı bir yüz analizi ve altın oran hesaplama sistemidir. Sistem, monolitik yapıların hantallığından uzak, birbirleriyle izole şekilde HTTP/JSON protokolü üzerinden haberleşen **Dağıtık Mikroservis Mimarisi** mimarisi üzerine kurgulanmıştır.

* **Mobil GUI (İstemci):** Native Android (Java) mimarisiyle geliştirilmiştir. Kullanıcıdan alınan fotoğrafları ağ katmanında (`OkHttp/Retrofit`) işleyerek Java API'ye asenkron olarak post eder.
* **Api ve Koordinasyon:** Java Spring Boot mimarisiyle geliştirilmiş ana merkezdir. Süreç orkestrasyonunu, veri güvenliğini, generic yanıt yönetimini ve izole veritabanı loglamalarını üstlenir.
* **Yapay Zeka Motoru (Mikroservis):** Python (FastAPI) tabanlı, donanım seviyesinde matris hesaplamaları yapan matematik motorudur. `MediaPipe Face Landmarker` kullanarak yüzdeki 468 koordinat noktasını ve burun eğimi gibi fizyognomik ölçümleri hesaplar.

### Mimarinin Görsel Akış Şeması (Mermaid Sequence Diagram)

```mermaid
sequenceDiagram
    autonumber
    actor Kullanıcı as 📱 Android Mobil GUI (Java)
    participant JavaAPI as ☕ Java Spring Boot API
    participant PythonAI as 🐍 Python FastAPI (MediaPipe)
    participant MongoDB as 🍃 NoSQL (MongoDB Atlas)
    database H2 as 💾 JDBC (H2 Database)

    Kullanıcı->>JavaAPI: POST /api/full-analysis (Multipart File)
    Note over JavaAPI: RestTemplate ve Timeout Koruması Devreye Girer
    JavaAPI->>PythonAI: HTTP POST /analyze-face (Görsel Aktarımı)
    Note over PythonAI: MediaPipe ile 468 Nokta Analizi Altın Oran Hesaplama
    PythonAI-->>JavaAPI: HTTP 200 OK (Ham JSON Verisi)
    
    par Veri İzolasyon Kayıtları (Eşzamanlı Dağıtım)
        JavaAPI->>MongoDB: save(FaceAnalysisResult)
        JavaAPI->>H2: save(RequestLog)
    end

    JavaAPI-->>Kullanıcı: HTTP 200 OK (Generic ApiResponse)
    ```