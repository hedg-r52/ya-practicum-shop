package ru.yandex.practicum.shop.service;

public interface CartService {
    void addProduct(Long productId);

    Integer getProductQuantity(Long productId);

    void updateQuantity(Long productId, Integer quantity);

    void removeProduct(Long productId);
}
