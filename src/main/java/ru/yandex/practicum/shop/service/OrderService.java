package ru.yandex.practicum.shop.service;

import ru.yandex.practicum.shop.entity.Order;

import java.util.Optional;

public interface OrderService {
    Optional<Order> findLastActiveOrder();
}
