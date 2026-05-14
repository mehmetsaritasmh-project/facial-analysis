package com.yuzanalizi.mobil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GoldenRatioActivity extends AppCompatActivity {

    private ImageView imgGoldenPreview;
    private TextView txtGoldenResult;
    private RelativeLayout layoutProgress;
    private ProgressBar progressBarGolden;
    private TextView txtProgressLabel;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_golden_ratio);

        imgGoldenPreview = findViewById(R.id.imgGoldenPreview);
        txtGoldenResult = findViewById(R.id.txtGoldenResult);
        layoutProgress = findViewById(R.id.layoutProgress);
        progressBarGolden = findViewById(R.id.progressBarGolden);
        txtProgressLabel = findViewById(R.id.txtProgressLabel);

        findViewById(R.id.btnGoldenSelect).setOnClickListener(v -> showImageSourceDialog());
        findViewById(R.id.btnGoldenAnalyze).setOnClickListener(v -> analyzeGoldenRatio());
    }

    private void showImageSourceDialog() {
        String[] options = {"Kameradan Çek", "Galeriden Seç"};
        new AlertDialog.Builder(this)
                .setTitle("Görsel Seçin")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        pickImage.launch("image/*");
                    }
                }).show();
    }

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgGoldenPreview.setImageURI(uri);
                    resetScreen();
                }
            }
    );

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePicture.launch(intent);
    }

    private final ActivityResultLauncher<Intent> takePicture = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
                    imgGoldenPreview.setImageBitmap(imageBitmap);
                    Toast.makeText(this, "Fotoğraf alındı.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void resetScreen() {
        txtGoldenResult.setVisibility(View.VISIBLE);
        layoutProgress.setVisibility(View.GONE);
        txtGoldenResult.setText("Analiz edilmeye hazır...");
    }

    private void analyzeGoldenRatio() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Lütfen önce bir görsel seçin", Toast.LENGTH_SHORT).show();
            return;
        }

        txtGoldenResult.setText("Altın oran hesaplanıyor...");

        String API_URL = "https://jujitsu-domain-onset.ngrok-free.dev";

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);

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
                    runOnUiThread(() -> updateUI(responseData));
                } else {
                    runOnUiThread(() -> txtGoldenResult.setText("Hata Kodu: " + response.code()));
                }
            } catch (Exception e) {
                runOnUiThread(() -> txtGoldenResult.setText("Bağlantı hatası oluştu."));
            }
        }).start();
    }

    private void updateUI(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (json.has("olcumler")) {
                JSONObject olcumler = json.getJSONObject("olcumler");

                if (olcumler.has("altin_oran_uygunluk_yuzdesi")) {
                    double yuzde = olcumler.getDouble("altin_oran_uygunluk_yuzdesi");
                    String durum = olcumler.optString("altin_oran_durum", "");

                    txtGoldenResult.setVisibility(View.GONE);
                    layoutProgress.setVisibility(View.VISIBLE);

                    int progressValue = (int) Math.round(yuzde);
                    progressBarGolden.setProgress(progressValue);

                    String label = String.format("%%%.2f Altın Oran Uyumu\nDurum: %s", yuzde, durum);
                    txtProgressLabel.setText(label);
                    return;
                }
            }

            txtGoldenResult.setVisibility(View.VISIBLE);
            layoutProgress.setVisibility(View.GONE);
            txtGoldenResult.setText("Altın oran verisi bulunamadı.");

        } catch (Exception e) {
            txtGoldenResult.setVisibility(View.VISIBLE);
            layoutProgress.setVisibility(View.GONE);
            txtGoldenResult.setText("Veri okuma hatası.");
        }
    }
}