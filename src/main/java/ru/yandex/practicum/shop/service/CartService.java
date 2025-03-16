package ru.yandex.practicum.shop.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.entity.Order;


public interface CartService {

    Mono<Order> getCart();

    Mono<Void> addProduct(Long productId);

    Mono<Void> updateQuantity(Long productId, Integer quantity);

    Mono<Void> removeProduct(Long productId);

    Mono<Void> moveCartToCheckout(Long orderId);

    Mono<Void> confirmPurchase(Long orderId);
}
