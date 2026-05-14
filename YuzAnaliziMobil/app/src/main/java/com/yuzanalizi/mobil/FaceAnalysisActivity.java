    package com.yuzanalizi.mobil;
    
    import java.util.Locale;
    import android.content.ContentValues;
    import android.content.Intent;
    import android.net.Uri;
    import android.os.Bundle;
    import android.provider.MediaStore;
    import android.view.View;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;
    import android.widget.ImageView;
    
    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.appcompat.app.AppCompatActivity;
    
    import org.json.JSONObject;
    
    import java.io.ByteArrayOutputStream;
    import java.io.InputStream;
    import java.util.HashMap;
    import java.util.Iterator;
    import java.util.Map;
    import java.util.concurrent.TimeUnit;
    
    import okhttp3.MediaType;
    import okhttp3.MultipartBody;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.RequestBody;
    import okhttp3.Response;
    
    public class FaceAnalysisActivity extends AppCompatActivity {
    
        private ImageView imgPreview;
        private TextView txtSummary;
        private LinearLayout layoutDetails;
        private Uri imageUri;
    
        private final Map<String, Double> measurements = new HashMap<>();
        private final Map<String, String> labels = new HashMap<>();
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_face_analysis);
    
            imgPreview = findViewById(R.id.imgPreview);
            txtSummary = findViewById(R.id.txtSummary);
            layoutDetails = findViewById(R.id.layoutDetails);
    
            findViewById(R.id.btnGallery).setOnClickListener(v -> pickImage.launch("image/*"));
            findViewById(R.id.btnCamera).setOnClickListener(v -> openCamera());
            findViewById(R.id.btnAnalyze).setOnClickListener(v -> analyzeFace());
        }
    
        private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imgPreview.setImageURI(uri);
                        clearPreviousResults();
                    }
                }
        );
    
        private final ActivityResultLauncher<Intent> takePicture = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && imageUri != null) {
                        imgPreview.setImageURI(imageUri);
                        clearPreviousResults();
                    }
                }
        );
    
        private void openCamera() {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Yeni Yuz Foto");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Kameradan Alindi");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            takePicture.launch(intent);
        }
    
        private void clearPreviousResults() {
            txtSummary.setText("Analiz edilmeye hazır...");
            layoutDetails.removeAllViews();
            measurements.clear();
            labels.clear();
        }
    
        private void analyzeFace() {
            if (imageUri == null) {
                Toast.makeText(this, "Lütfen önce resim seçin veya çekin", Toast.LENGTH_SHORT).show();
                return;
            }
    
            txtSummary.setText("Yüz analiz ediliyor, lütfen bekleyin...");
            String API_URL = "https://jujitsu-domain-onset.ngrok-free.dev/api/full-analysis";
    
            new Thread(() -> {
                try {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .build();
    
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }
                    byte[] bytes = byteBuffer.toByteArray();
    
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", "image.jpg",
                                    RequestBody.create(bytes, MediaType.parse("image/jpeg")))
                            .build();
    
                    Request request = new Request.Builder().url(API_URL).post(requestBody).build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
    
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> parseAndDisplay(responseData));
                    } else {
                        runOnUiThread(() -> txtSummary.setText("Sunucu hatası: " + response.code()));
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        txtSummary.setText("Veri işleme / Bağlantı hatası: " + e.getMessage());
                        Toast.makeText(FaceAnalysisActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        }
    
        private void parseAndDisplay(String jsonStr) {
            try {
                JSONObject json = new JSONObject(jsonStr);
                JSONObject dataJson;
                if (json.has("data") && json.get("data") instanceof String) {
                    dataJson = new JSONObject(json.getString("data"));
                } else if (json.has("data")) {
                    dataJson = json.getJSONObject("data");
                } else {
                    dataJson = json;
                }
    
                JSONObject olcumlerJson = dataJson.getJSONObject("olcumler");
                JSONObject etiketlerJson = dataJson.getJSONObject("etiketler");
    
                measurements.clear();
                labels.clear();
    
                Iterator<String> keysOlcum = olcumlerJson.keys();
                while (keysOlcum.hasNext()) {
                    String key = keysOlcum.next();
                    Object val = olcumlerJson.get(key);
                    if (val instanceof Number) {
                        measurements.put(key, ((Number) val).doubleValue());
                    }
                }
    
                Iterator<String> keysEtiket = etiketlerJson.keys();
                while (keysEtiket.hasNext()) {
                    String key = keysEtiket.next();
                    labels.put(key, etiketlerJson.getString(key));
                }
    
                StringBuilder summaryBuilder = new StringBuilder();
                summaryBuilder.append("✨ YÜZ KARAKTER ANALİZİ ✨\n");
                summaryBuilder.append("───────────────────────────\n\n");
    
                // ==========================================
                // 👃 1. BURUN YAPISI ANALİZLERİ
                // ==========================================
                if (labels.containsKey("nose_width") && measurements.containsKey("nose_width")) {
                    String etiket = labels.get("nose_width").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("nose_width"));
                    summaryBuilder.append("👃 Burun Genişliği (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• İnsanlarla empati kurma gücün yüksek, nazik ve düşünceli birisin. Ancak bazen karar verirken çekingen davranabilirsin.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Kendini ifade etmekten çekinmeyen, güçlü karakterli birisin ama bazen fazla dominant olabilirsin.\n\n");
                    } else {
                        summaryBuilder.append("• Kararlarında dengeli, sosyal ilişkilerde ölçülüsün fakat bazen kararsız kalabiliyorsun.\n\n");
                    }
                }
    
                if (labels.containsKey("nose_length") && measurements.containsKey("nose_length")) {
                    String etiket = labels.get("nose_length").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("nose_length"));
                    summaryBuilder.append("📐 Burun Uzunluğu (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• Bazen temkinli davranırsın, bu dikkatli yapının işaretidir fakat risk almaktan çekinmen gelişimini engelleyebilir.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Zaman zaman çevreni anlamakta zorlanabilirsin, bu seni iç gözlemci yapar fakat iletişim sorunlarına yol açabilir.\n\n");
                    } else {
                        summaryBuilder.append("• Sağduyun gelişmiş ve olaylara dengeli yaklaşıyorsun ama bazen aşırı temkinli oluyorsun.\n\n");
                    }
                }
    
                if (labels.containsKey("burun_kalkikligi") && measurements.containsKey("burun_kalkikligi")) {
                    String etiket = labels.get("burun_kalkikligi").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("burun_kalkikligi"));
                    summaryBuilder.append("📈 Burun Eğimi / Kalkıklığı (").append(fmt).append("°):\n");
                    if (etiket.contains("kalkık") || etiket.contains("raised") || etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Girdiğin ortamda dikkat çeken, özgüvenli ve liderlik özellikleri olan birisin ama bazen dik başlı olabilirsin.\n\n");
                    } else if (etiket.contains("düşük") || etiket.contains("low") || etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• Uyumlu, sakin ve karşı tarafı dinleyen bir yapın var ancak bazen kararsız ve çekingen kalabilirsin.\n\n");
                    } else {
                        summaryBuilder.append("• Hayatın çoğu alanında dengeli bir duruş sergiliyorsun ama bazen durağan ve motivasyonsuz olabiliyorsun.\n\n");
                    }
                }
    
                // ==========================================
                // 👄 2. AĞIZ VE DUDAK ANALİZLERİ
                // ==========================================
                if (labels.containsKey("lip_width") && measurements.containsKey("lip_width")) {
                    String etiket = labels.get("lip_width").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("lip_width"));
                    summaryBuilder.append("👄 Dudak Genişliği (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• Duygularını hemen belli etmeyen, derin düşüncelere sahip birisin fakat bazen soğuk ve mesafeli görünürsün.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Sosyal ortamlarda öne çıkmayı seven, cesur bir yapıya sahipsin ama bazen fazla konuşkan olabilirsin.\n\n");
                    } else {
                        summaryBuilder.append("• Açık fikirli ama dengeli bir iletişim tarzın var, bazen kararsızlık yaşayabilirsin.\n\n");
                    }
                }
    
                if (labels.containsKey("upper_lip_height") && measurements.containsKey("upper_lip_height")) {
                    String etiket = labels.get("upper_lip_height").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("upper_lip_height"));
                    summaryBuilder.append("🔼 Üst Dudak Kalınlığı (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• İnsanları dinlemeyi bilen, anlayışlı biri olduğunu gösteriyor ancak kendi düşüncelerini gizleyebilirsin.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Fikirlerini paylaşmaktan çekinmeyen, açık sözlü birisin bazen fazla agresif olabilirsin.\n\n");
                    } else {
                        summaryBuilder.append("• Kendini ifade ederken ne fazla konuşkansın ne de suskunsun, ama bazen net olmayabilirsin.\n\n");
                    }
                }
    
                if (labels.containsKey("lower_lip_height") && measurements.containsKey("lower_lip_height")) {
                    String etiket = labels.get("lower_lip_height").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("lower_lip_height"));
                    summaryBuilder.append("🔽 Alt Dudak Kalınlığı (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• Duygularını kontrol etme becerin yüksek; sakinliğinle dikkat çekiyorsun ama bazen içine kapanıyorsun.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Etkileyici ve kendine güvenen bir dışavurumun var bazen fazla kendini öne çıkarıyorsun.\n\n");
                    } else {
                        summaryBuilder.append("• Ne içe kapanıksın ne de aşırı dışa dönük, dengeli bir duruşun var fakat bazen kararsızsın.\n\n");
                    }
                }
    
                // ==========================================
                // 👁️ 3. GÖZ YAPISI ANALİZLERİ
                // ==========================================
                if (labels.containsKey("eye_distance") && measurements.containsKey("eye_distance")) {
                    String etiket = labels.get("eye_distance").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("eye_distance"));
                    summaryBuilder.append("👁️ Göz Arası Mesafe (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• Detaylara gösterdiğin özen, seni analiz gücü yüksek biri yapıyor fakat bazen aşırı eleştirel olabiliyorsun.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Hayata farklı açılardan bakan, özgür düşünceli bir kişiliğe sahipsin ama bazen dağınık ve odaklanamıyorsun.\n\n");
                    } else {
                        summaryBuilder.append("• Ne fazla analizci ne de yüzeysel — olaylara dengeli bakabiliyorsun ama bazen kararsızsın.\n\n");
                    }
                }
    
                if (labels.containsKey("left_eye_width") && measurements.containsKey("left_eye_width")) {
                    String etiket = labels.get("left_eye_width").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("left_eye_width"));
                    summaryBuilder.append("👁️ Sol Göz Genişliği (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• Bazen kendi kabuğuna çekilmeyi tercih edersin ama bu seni daha dikkatli yapar, bazen sosyal ortamdan uzak duruyorsun.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• İnsanlarla rahat iletişim kuran, açık ve canlı bir enerjiye sahipsin fakat bazen fazla agresif oluyorsun.\n\n");
                    } else {
                        summaryBuilder.append("• Gözlerin, dengeli bir ruh halini ve iç uyumu yansıtıyor, bazen duygusal dalgalanmalar yaşayabilirsin.\n\n");
                    }
                }
    
                if (labels.containsKey("right_eye_width") && measurements.containsKey("right_eye_width")) {
                    String etiket = labels.get("right_eye_width").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("right_eye_width"));
                    summaryBuilder.append("👁️ Sağ Göz Genişliği (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• İnsanların tepkilerini gözlemlemeyi seven, temkinli bir yapın olabilir ama bazen aşırı şüpheci oluyorsun.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Kendine güvenin gözlerinden okunuyor; insanlarla etkili iletişim kuruyorsun ama bazen fazla dominantsın.\n\n");
                    } else {
                        summaryBuilder.append("• Göz temasın dengeli ve samimi, bazen duygularını gizliyorsun.\n\n");
                    }
                }
    
                if (labels.containsKey("left_eye_height") && measurements.containsKey("left_eye_height")) {
                    String etiket = labels.get("left_eye_height").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("left_eye_height"));
                    summaryBuilder.append("👁️ Sol Göz Açıklığı (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• İç dünyan zengin, duygularını derinlemesine yaşıyorsun ama bazen kendini kapatıyorsun.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Hayat enerjin yüzünden okunuyor; neşeli ve dışa dönüksün bazen fazla sabırsızsın.\n\n");
                    } else {
                        summaryBuilder.append("• Göz temasın ölçülü; ne fazla bastıran ne de kaçan, bazen duygularını saklıyorsun.\n\n");
                    }
                }
    
                if (labels.containsKey("right_eye_height") && measurements.containsKey("right_eye_height")) {
                    String etiket = labels.get("right_eye_height").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("right_eye_height"));
                    summaryBuilder.append("👁️ Sağ Göz Açıklığı (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• İnsanlarla mesafeni iyi ayarlayan, kendi iç huzuruna önem veren birisin ama bazen soğuk olabiliyorsun.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Sıcakkanlı ve pozitif bir yaklaşımın var; çevrendekileri kolayca etkilersin ama bazen aceleci oluyorsun.\n\n");
                    } else {
                        summaryBuilder.append("• Gözlerin uyumlu ve dengeli bir karakteri yansıtıyor fakat bazen kararsızsın.\n\n");
                    }
                }
    
                // ==========================================
                // 👤 4. YÜZ FORMU ANALİZLERİ
                // ==========================================
                if (labels.containsKey("brow_distance") && measurements.containsKey("brow_distance")) {
                    String etiket = labels.get("brow_distance").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("brow_distance"));
                    summaryBuilder.append("🤨 Kaş Arası Mesafe (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• Belirlediğin hedeflere odaklanma gücün çok yüksek ama bazen esnekliğin düşük.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Hoşgörülü, rahat ve sabırlı bir yapın var bazen fazla pasif kalıyorsun.\n\n");
                    } else {
                        summaryBuilder.append("• Hayata dengeli bakıyor, kararlarını sakin alıyorsun bazen kararsız olabiliyorsun.\n\n");
                    }
                }
    
                if (labels.containsKey("cheek_width") && measurements.containsKey("cheek_width")) {
                    String etiket = labels.get("cheek_width").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("cheek_width"));
                    summaryBuilder.append("👤 Yanak Genişliği (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• Nazik, kırılgan ama insanlara derin bağlar kuran birisin bazen fazla hassassın.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Güçlü duruşunla çevrenin güvenini kolayca kazanırsın bazen otoriter oluyorsun.\n\n");
                    } else {
                        summaryBuilder.append("• Ruhsal anlamda dengeli, duygularını ölçülü yaşayan bir yapın var bazen içe kapanık oluyorsun.\n\n");
                    }
                }
    
                if (labels.containsKey("forehead_height") && measurements.containsKey("forehead_height")) {
                    String etiket = labels.get("forehead_height").toLowerCase(Locale.ROOT);
                    String fmt = String.format(Locale.US, "%.2f", measurements.get("forehead_height"));
                    summaryBuilder.append("👱 Alın Yüksekliği (").append(fmt).append(" px):\n");
                    if (etiket.contains("küçük") || etiket.contains("small")) {
                        summaryBuilder.append("• Hızlı kararlar alma ve çözüm üretme becerin dikkat çekici ama bazen aceleci davranıyorsun.\n\n");
                    } else if (etiket.contains("büyük") || etiket.contains("large")) {
                        summaryBuilder.append("• Derin düşünmeyi seven, stratejik bir zihne sahipsin ama bazen aşırı mükemmeliyetçisin.\n\n");
                    } else {
                        summaryBuilder.append("• Analitik düşünce ile sezgilerini dengeli kullanıyorsun fakat bazen kararsızsın.\n\n");
                    }
                }
    
                // --- 🎨 GÖRSEL TASARIM AYARLARI ---
                txtSummary.setText(summaryBuilder.toString().trim());
                txtSummary.setTextColor(0xFF222222);
                txtSummary.setTextSize(15);
                txtSummary.setLineSpacing(6, 1.3f);
                txtSummary.setBackgroundColor(0xFFFFFFFF);
    
                int paddingPx = (int) (18 * getResources().getDisplayMetrics().density);
                txtSummary.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    
                if (layoutDetails != null) {
                    layoutDetails.removeAllViews();
                    layoutDetails.setVisibility(View.GONE);
                }
    
            } catch (Exception e) {
                e.printStackTrace();
                txtSummary.setText("Veri işleme hatası: " + e.getMessage());
            }
        }
    }