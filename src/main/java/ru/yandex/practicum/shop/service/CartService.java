package ru.yandex.practicum.shop.service;

import ru.yandex.practicum.shop.entity.Order;

import java.util.Optional;

public interface CartService {

    Optional<Order> getCart();

    void addProduct(Long productId);

    void updateQuantity(Long productId, Integer quantity);

    void removeProduct(Long productId);

    void moveCartToCheckout(Long orderId);

    void confirmPurchase(Long orderId);
}
