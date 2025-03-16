package ru.yandex.practicum.shop.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.mapper.OrderMapper;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public Mono<Order> findLastActiveOrder() {
        return orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE);
    }

    @Override
    public Mono<Order> findByIdAndStatus(Long id, OrderStatus status) {
        return orderRepository.findByIdAndStatus(id, status);
    }

    @Override
    public Mono<Page<OrderDto>> findAll(Pageable pageable) {
        return orderRepository.findAllBy(pageable)
                .flatMap(order -> Mono.just(orderMapper.toOrderDto(order)))
                .collectList()
                .zipWith(orderRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));

    }
}
