package ru.yandex.practicum.shop.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {

    Mono<Order> findFirstByStatusOrderByCreatedAtDesc(OrderStatus status);

    Mono<Order> findByIdAndStatus(Long id, OrderStatus status);

    Flux<Order> findAllBy(Pageable pageable);
}
