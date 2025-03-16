package ru.yandex.practicum.shop.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.repository.OrderItemRepository;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.CartService;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Mono<Order> getCart() {
        return orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE);
    }

    @Transactional
    @Override
    public Mono<Void> addProduct(Long productId) {
        return orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE)
                .flatMap(order -> {
                    var productExists = order.getItems().stream()
                            .map(OrderItem::getProduct)
                            .map(Product::getId)
                            .anyMatch(id -> id.equals(productId));
                    if (productExists) {
                        return Mono.error(new IllegalArgumentException("Продукт с ID " + productId + " уже добавлен в заказ"));
                    }

                    return productRepository.findById(productId)
                            .flatMap(product -> {
                                var orderItem = OrderItem.builder()
                                        .product(product)
                                        .order(order)
                                        .quantity(1)
                                        .build();

                                order.getItems().add(orderItem);
                                return orderRepository.save(order)
                                        .then(orderItemRepository.save(orderItem));
                            });
                })
                .then();
    }

    @Transactional
    @Override
    public Mono<Void> updateQuantity(Long productId, Integer delta) {
        return orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE)
                .flatMap(order -> {
                    var orderItem = order.getItems().stream()
                            .filter(oi -> productId.equals(oi.getProduct().getId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Не найдена позиция товара в заказе."));

                    int newQuantity = orderItem.getQuantity() + delta;
                    if (newQuantity == 0) {
                        order.getItems().remove(orderItem);
                        return orderRepository.save(order);
                    } else {
                        orderItem.setQuantity(newQuantity);
                        return orderItemRepository.save(orderItem)
                                .then(orderRepository.save(order));
                    }
                })
                .then();
    }

    @Transactional
    @Override
    public Mono<Void> removeProduct(Long productId) {
        return orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE)
                .flatMap(order -> {
                    var orderItem = order.getItems().stream()
                            .filter(oi -> productId.equals(oi.getProduct().getId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Не найдена позиция товара в заказе."));

                    order.getItems().remove(orderItem);
                    return orderRepository.save(order);
                })
                .then();
    }

    @Override
    public Mono<Void> moveCartToCheckout(Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Нет активного заказа. Невозможно оформить заказ")))
                .flatMap(order -> {
                    order.setStatus(OrderStatus.CHECKOUT);
                    return orderRepository.save(order);
                })
                .then();
    }

    @Override
    public Mono<Void> confirmPurchase(Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Нет активного заказа. Невозможно подтвердить оплату заказа")))
                .flatMap(order -> {
                    order.setStatus(OrderStatus.PAID);
                    return orderRepository.save(order); // Асинхронное сохранение заказа с новым статусом
                })
                .then();
    }
}
