package com.bhagat.redis.service;

import com.bhagat.redis.model.Order;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;

@Service
public class OrderService {
    @Cacheable("order-by-id")
    public Order byId(Long id) {
        try {
            Thread.sleep(1000 * 10);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return new Order(id, new Date(), Collections.emptyList());
    }
}
