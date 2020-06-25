package com.bhagat.redis;

import lombok.extern.java.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Log
@EnableCaching
@EnableRedisHttpSession
@SpringBootApplication
public class RedisAppMain {

    public static void main(String[] args) {
        SpringApplication.run(RedisAppMain.class, args);
    }
}












