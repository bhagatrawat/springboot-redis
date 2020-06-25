package com.bhagat.redis.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class ShoppingCart implements Serializable {
    private final Collection<Order> orders = new ArrayList<>();

    public void addOrder(Order order) {
        this.orders.add((order));
    }

    public Collection<Order> getOrders() {
        return this.orders;
    }
}