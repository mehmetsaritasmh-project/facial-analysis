package com.yuzanalizi.yuzanalizapi.controller;

import com.yuzanalizi.yuzanalizapi.dto.ApiResponse; // Burası önemli: dto içindeki ortak yapıyı çağırıyoruz
import com.yuzanalizi.yuzanalizapi.model.User;
import com.yuzanalizi.yuzanalizapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // 1. KULLANICI OLUŞTURMA / KAYIT OLMA (DATABASE'E YAZAN KISIM)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        
        if (existingUser != null) {
            // HATA: 400 Bad Request dönüyoruz, veri kısmı null
            ApiResponse<User> response = new ApiResponse<>(false, "Bu kullanıcı adı zaten alınmış!", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        // BAŞARILI: 201 Created dönüyoruz, kaydedilen kullanıcıyı içine gömüyoruz
        User savedUser = userRepository.save(user);
        ApiResponse<User> response = new ApiResponse<>(true, "Kullanıcı başarıyla oluşturuldu!", savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. GİRİŞ YAPMA (DATABASE'DEN KONTROL EDEN KISIM)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody User user) {
        User foundUser = userRepository.findByUsername(user.getUsername());
        
        if (foundUser != null && foundUser.getPassword().equals(user.getPassword())) {
            // BAŞARILI: 200 OK dönüyoruz, giriş yapan kullanıcı verisini uçuruyoruz
            ApiResponse<User> response = new ApiResponse<>(true, "Giriş Başarılı!", foundUser);
            return ResponseEntity.ok(response);
        } else {
            // HATA: 401 Unauthorized dönüyoruz
            ApiResponse<User> response = new ApiResponse<>(false, "Kullanıcı adı veya şifre hatalı!", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}