package com.yuzanalizi.yuzanalizapi.repository;

import com.yuzanalizi.yuzanalizapi.model.FaceAnalysisResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaceAnalysisMongoRepository extends MongoRepository<FaceAnalysisResult, String> {
}