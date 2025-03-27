package ru.yandex.practicum.shop.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.exception.ResourceNotFoundException;
import ru.yandex.practicum.shop.mapper.OrderItemMapper;
import ru.yandex.practicum.shop.repository.OrderItemRepository;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.CartService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.shop.util.OrderUtil.buildOrderDto;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    public static final String PRODUCT_NOT_FOUND = "Продукт не найден.";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;
    private final CacheManager cacheManager;

    @Override
    public Mono<OrderDto> getCart() {
        return Mono.defer(() -> orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE)
                    .flatMap(order -> {
                        String cacheKey = String.valueOf(order.getId());

                        Cache cache = cacheManager.getCache("cart");
                        if (cache == null) return getCartAndCache(order);

                        Cache.ValueWrapper cachedValueWrapper = cache.get(cacheKey);

                        if (cachedValueWrapper != null) {
                            Object cachedValue = cachedValueWrapper.get();
                            if (cachedValue instanceof LinkedHashMap<?, ?> cachedMap) {
                                return Mono.just(convertMapToOrderDto(cachedMap));
                            }
                        }

                        return getCartAndCache(order);
                    })
        );
    }

    private Mono<OrderDto> getCartAndCache(Order order) {
        return orderItemRepository.findAllByOrderId(
                        order.getId(),
                        Sort.by("id").ascending()
                )
                .collectList()
                .flatMap(items -> {
                    List<Long> productIds = items.stream().map(OrderItem::getProductId).toList();
                    return getProductMap(productIds)
                            .map(productMap -> buildOrderDto(order, orderItemMapper.map(items), productMap));
                })
                .doOnNext(orderDto -> {
                    Cache cache = cacheManager.getCache("cart");
                    if (cache != null) {
                        String cacheKey = String.valueOf(orderDto.getId());
                        cache.put(cacheKey, orderDto);  // Кешируем по ключу id активного заказа
                    }
                });
    }

    @Transactional
    @Override
    public Mono<Void> addProduct(Long productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(PRODUCT_NOT_FOUND)))
                .flatMap(product -> orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE)
                        .switchIfEmpty(createNewActiveOrder())
                        .flatMap(order -> orderItemRepository.findByOrderIdAndProductId(order.getId(), productId)
                                .flatMap(orderItem ->
                                        Mono.error(new IllegalArgumentException(
                                                "Продукт с ID " + productId + " уже добавлен в заказ"
                                        ))
                                )
                                .switchIfEmpty(Mono.defer(() -> {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setOrderId(order.getId());
                                    orderItem.setProductId(productId);
                                    orderItem.setQuantity(1);
                                    return orderItemRepository.save(orderItem);
                                }))
                        )
                )
                .then();
    }

    @Transactional
    @Override
    public Mono<Void> updateQuantity(Long productId, Integer delta) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(PRODUCT_NOT_FOUND)))
                .flatMap(product -> orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE)
                        .flatMap(order -> orderItemRepository.findByOrderIdAndProductId(order.getId(), productId)
                                .flatMap(orderItem -> {
                                    int newQuantity = orderItem.getQuantity() + delta;
                                    if (newQuantity == 0) {
                                        return orderItemRepository.delete(orderItem);
                                    } else {
                                        orderItem.setQuantity(newQuantity);
                                        return orderItemRepository.save(orderItem);
                                    }
                                })
                        )
                )
                .then();
    }

    @Transactional
    @Override
    public Mono<Void> removeProduct(Long productId) {
        return orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Активный заказ не найден")))
                .flatMap(order -> productRepository.findById(productId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Продукт не найден")))
                        .flatMap(product -> orderItemRepository.findByOrderIdAndProductId(order.getId(), productId)
                                .flatMap(orderItemRepository::delete)
                                .then()));
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
                    return orderRepository.save(order);
                })
                .then();
    }

    private Mono<Order> createNewActiveOrder() {
        Order newOrder = new Order();
        newOrder.setStatus(OrderStatus.ACTIVE);
        newOrder.setCreatedAt(LocalDate.now());
        return orderRepository.save(newOrder);
    }

    private Mono<Map<Long, ProductDto>> getProductMap(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Mono.just(Map.of());
        }
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

    private OrderDto convertMapToOrderDto(Map<?, ?> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(map, OrderDto.class);
    }
}
