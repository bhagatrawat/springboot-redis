package com.bhagat.redis.repository;

import com.bhagat.redis.model.Order;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Date;

public interface OrderRepository extends CrudRepository<Order, Long> {
    Collection<Order> findByWhen(Date d);
}
