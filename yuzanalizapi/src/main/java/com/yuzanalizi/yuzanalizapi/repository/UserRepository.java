package com.yuzanalizi.yuzanalizapi.repository;

import com.yuzanalizi.yuzanalizapi.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    // Kullanıcı adından arama yapabilmek için gerekirse ileride kullanırız
    User findByUsername(String username);
}