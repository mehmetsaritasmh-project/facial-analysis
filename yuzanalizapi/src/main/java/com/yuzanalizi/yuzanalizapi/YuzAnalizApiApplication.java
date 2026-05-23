package com.yuzanalizi.yuzanalizapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
// MongoDB ve JPA/JDBC depolarını ayrı ayrı tanıtıyoruz
@EnableJpaRepositories(basePackages = "com.yuzanalizi.yuzanalizapi.repository")
@EnableMongoRepositories(basePackages = "com.yuzanalizi.yuzanalizapi.repository")
public class YuzAnalizApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuzAnalizApiApplication.class, args);
    }
}