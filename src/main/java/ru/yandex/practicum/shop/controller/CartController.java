package ru.yandex.practicum.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.shop.service.CartService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<Map<String, Object>> addToCart(@PathVariable Long productId) {
        cartService.addProduct(productId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update/{productId}")
    public ResponseEntity<Map<String, Object>> updateQuantity(@PathVariable Long productId, @RequestParam int quantity) {
        cartService.updateQuantity(productId, quantity);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("quantity", cartService.getProductQuantity(productId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove/{productId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long productId) {
        cartService.removeProduct(productId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

}
