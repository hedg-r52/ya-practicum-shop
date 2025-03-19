package ru.yandex.practicum.shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.dto.OrderItemDto;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.exception.ResourceNotFoundException;
import ru.yandex.practicum.shop.mapper.OrderItemMapper;
import ru.yandex.practicum.shop.repository.OrderItemRepository;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.OrderService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.yandex.practicum.shop.util.OrderUtil.buildOrderDto;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;

    @Override
    public Mono<OrderDto> findLastActiveOrder() {
        return orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE)
                .flatMap(order -> findOrderItemsByOrderId(order.getId())
                        .flatMap(items -> {
                            List<Long> productIds = items.stream().map(OrderItemDto::getProductId).toList();
                            return getProductMap(productIds)
                                    .map(productMap -> buildOrderDto(order, items, productMap));
                        })
                );
    }

    @Override
    public Mono<OrderDto> findByIdAndStatus(Long id, OrderStatus status) {
        return orderRepository.findByIdAndStatus(id, status)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Заказ с id=" + id + " и статусом=" + status.name() + " не найден")))
                .flatMap(order -> findOrderItemsByOrderId(order.getId())
                        .flatMap(items -> {
                            List<Long> productIds = items.stream().map(OrderItemDto::getProductId).toList();
                            return getProductMap(productIds)
                                    .map(productMap -> buildOrderDto(order, items, productMap));
                        })
                );
    }

    @Override
    public Mono<Page<OrderDto>> findAll(Pageable pageable) {
        return orderRepository.findAllBy(pageable)
                .collectList()
                .flatMap(orders -> {
                    List<Long> orderIds = orders.stream()
                            .map(Order::getId)
                            .toList();

                    return findOrderItemsByOrderIdList(orderIds)
                            .flatMap(orderItems -> {
                                Map<Long, List<OrderItemDto>> itemsMap = orderItems.stream()
                                        .collect(Collectors.groupingBy(OrderItemDto::getOrderId));

                                List<Long> productIds = orderItems.stream()
                                        .map(OrderItemDto::getProductId)
                                        .distinct()
                                        .toList();

                                return getProductMap(productIds)
                                        .map(productMap -> orders.stream()
                                                .map(order -> buildOrderDto(
                                                        order,
                                                        itemsMap.getOrDefault(order.getId(), List.of()),
                                                        productMap
                                                ))
                                                .toList()
                                        );
                            })
                            .zipWith(orderRepository.count())
                            .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
                });

    }

    private Mono<List<OrderItemDto>> findOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findAllByOrderId(orderId)
                .collectList()
                .map(orderItemMapper::map);
    }

    private Mono<List<OrderItemDto>> findOrderItemsByOrderIdList(List<Long> orderIds) {
        return orderItemRepository.findAllByOrderIds(orderIds)
                .collectList()
                .map(orderItemMapper::map);
    }


    private Mono<Map<Long, ProductDto>> getProductMap(List<Long> productIds) {
        return productRepository.findAllById(productIds)
                .collectMap(
                        Product::getId,
                        product -> ProductDto.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .price(product.getPrice())
                                .build()
                );
    }
}
