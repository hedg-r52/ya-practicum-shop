package ru.yandex.practicum.shop.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {
    Flux<OrderItem> findAllByOrderId(Long orderId);

    @Query("SELECT oi.id, oi.order_id, oi.product_id, oi.quantity FROM order_items oi WHERE oi.order_id in (:orderIds)")
    Flux<OrderItem> findAllByOrderIds(List<Long> orderIds);

    Mono<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);
}
