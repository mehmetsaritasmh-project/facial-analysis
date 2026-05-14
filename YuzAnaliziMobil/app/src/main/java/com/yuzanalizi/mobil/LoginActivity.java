package com.yuzanalizi.mobil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private final String BASE_URL = "https://jujitsu-domain-onset.ngrok-free.dev/api/auth/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                sendAuthRequest(username, password, "login");
            } else {
                Toast.makeText(this, "Alanları doldur!", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                sendAuthRequest(username, password, "register");
            } else {
                Toast.makeText(this, "Alanları doldur!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendAuthRequest(String username, String password, String endpoint) {
        String url = BASE_URL + endpoint;
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String requestBody = jsonBody.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        // Gelen ham JSON string'ini işlenebilir objeye çeviriyoruz
                        JSONObject jsonResponse = new JSONObject(response);

                        // BURASI GÜNCELLENDİ: Java DTO'sundaki isimlerle birebir eşitledik
                        String mesaj = jsonResponse.getString("message"); // "message" yaptık
                        boolean basarili = jsonResponse.getBoolean("success"); // "success" yaptık

                        // Ekranda sadece backend'den gelen temiz mesajı gösteriyoruz
                        Toast.makeText(LoginActivity.this, mesaj, Toast.LENGTH_SHORT).show();

                        // Giriş isteği onaylandıysa ana sayfaya uçur
                        if (endpoint.equals("login") && basarili) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Sunucu yanıtı çözülemedi!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VOLLEY_ERROR", error.toString());
                    // HTTP 400 veya 401 hatası geldiğinde burası tetiklenir
                    Toast.makeText(LoginActivity.this, "Giriş başarısız veya bağlantı hatası!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return requestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        queue.add(stringRequest);
    }
}