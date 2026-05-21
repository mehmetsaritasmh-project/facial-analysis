package com.yuzanalizi.yuzanalizapi.repository;

import com.yuzanalizi.yuzanalizapi.model.RequestLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogJdbcRepository extends CrudRepository<RequestLog, Long> {
}