package com.yuzanalizi.mobil;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Arayüz elemanlarını tanımlama
        CardView cardFaceAnalysis = findViewById(R.id.cardFaceAnalysis);
        CardView cardGoldenRatio = findViewById(R.id.cardGoldenRatio);
        TextView btnHowItWorks = findViewById(R.id.btnHowItWorks);

        // "Nasıl Analiz Edilir?" yazısının altını çizmek için
        btnHowItWorks.setPaintFlags(btnHowItWorks.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Yüz Analizi Kartı Tıklama Olayı (Navigator.push karşılığı)
        cardFaceAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Not: FaceAnalysisActivity sınıfını projende oluşturmuş olman gerekiyor
                Intent intent = new Intent(MainActivity.this, FaceAnalysisActivity.class);
                startActivity(intent);
            }
        });

        // Altın Oran Kartı Tıklama Olayı
        cardGoldenRatio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Not: GoldenRatioActivity sınıfını projende oluşturmuş olman gerekiyor
                Intent intent = new Intent(MainActivity.this, GoldenRatioActivity.class);
                startActivity(intent);
            }
        });

        // Nasıl Çalışır Tıklama Olayı ve Dialog
        btnHowItWorks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHowItWorksDialog();
            }
        });
    }

    // Flutter'daki _showHowItWorksDialog metodunun karşılığı
    private void showHowItWorksDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.how_analyze_title)
                .setMessage(R.string.how_analyze_content)
                .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }
}