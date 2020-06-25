package com.bhagat.redis.repository;


import com.bhagat.redis.model.LineItem;
import org.springframework.data.repository.CrudRepository;

public interface LineItemRepository extends CrudRepository<LineItem, Long> {

}
