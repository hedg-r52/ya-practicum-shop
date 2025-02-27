package ru.yandex.practicum.shop.service.impl;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.service.OrderService;

import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Optional<Order> findLastActiveOrder() {
        return orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE);
    }

    @Override
    public Optional<Order> findByIdAndStatus(Long id, OrderStatus status) {
        return orderRepository.findByIdAndStatus(id, status);
    }
}
