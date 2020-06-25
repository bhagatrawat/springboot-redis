package com.bhagat.redis.controller;

import com.bhagat.redis.model.Order;
import com.bhagat.redis.model.ShoppingCart;
import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

@Log
@Controller
@SessionAttributes("cart")
public class CartSessionController {
    private final AtomicLong ids = new AtomicLong();

    @ModelAttribute("cart")
    private ShoppingCart cart() {
        log.info("Creating new Cart");
        return new ShoppingCart();
    }

    @GetMapping("/orders")
    String orders(@ModelAttribute("cart") ShoppingCart cart, Model model) {
        cart.addOrder(new Order(ids.incrementAndGet(), new Date(), Collections.emptyList()));
        model.addAttribute("orders", cart.getOrders());
        return "orders";
    }
}