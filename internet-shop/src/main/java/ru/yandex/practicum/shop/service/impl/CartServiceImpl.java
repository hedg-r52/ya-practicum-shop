package ru.yandex.practicum.shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.exception.NotEnoughMoneyException;
import ru.yandex.practicum.shop.exception.ResourceNotFoundException;
import ru.yandex.practicum.shop.mapper.OrderItemMapper;
import ru.yandex.practicum.shop.repository.OrderItemRepository;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.CartService;
import ru.yandex.practicum.shop.service.PaymentService;
import ru.yandex.practicum.shop.util.OrderUtil;
import ru.yandex.practicum.shop.util.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    public static final String PRODUCT_NOT_FOUND = "Продукт не найден.";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;
    private final CacheManager cacheManager;
    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    @Override
    public Mono<OrderDto> getCart() {
        return securityUtils.getUserId()
                .switchIfEmpty(Mono.error(new AccessDeniedException("Пользователь не аутентифицирован!")))
                .flatMap(userId -> orderRepository.findFirstByUserIdAndStatusOrderByCreatedAt(userId, OrderStatus.ACTIVE)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Активный заказ не найден")))
                        .flatMap(order -> {
                            String cacheKey = "user:" + userId + ":order:" + order.getId();

                            Cache cache = cacheManager.getCache("cart");
                            if (cache == null) return getCartAndCache(userId, order);

                            Cache.ValueWrapper cachedValueWrapper = cache.get(cacheKey);

                            if (cachedValueWrapper != null) {
                                Object cachedValue = cachedValueWrapper.get();
                                if (cachedValue instanceof OrderDto orderDto) {
                                    return Mono.just(orderDto);
                                }
                            }

                            return getCartAndCache(userId, order);
                        })
                );
    }

    private Mono<OrderDto> getCartAndCache(Long userId, Order order) {
        return orderItemRepository.findAllByOrderId(
                        order.getId(),
                        Sort.by("id").ascending()
                )
                .collectList()
                .flatMap(items -> {
                    List<Long> productIds = items.stream().map(OrderItem::getProductId).toList();
                    return getProductMap(productIds)
                            .map(productMap -> OrderUtil.buildOrderDto(order, orderItemMapper.map(items), productMap));
                })
                .doOnNext(orderDto -> {
                    Cache cache = cacheManager.getCache("cart");
                    if (cache != null) {
                        String cacheKey = "user:" + userId + ":order:" + orderDto.getId();
                        cache.put(cacheKey, orderDto);
                    }
                });
    }

    @Transactional
    @Override
    public Mono<Void> addProduct(Long productId) {
        return securityUtils.getUserId()
                .switchIfEmpty(Mono.error(new AccessDeniedException("Пользователь не аутентифицирован!")))
                .flatMap(userId ->
                        productRepository.findById(productId)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException(PRODUCT_NOT_FOUND)))
                                .flatMap(product -> orderRepository.findFirstByUserIdAndStatusOrderByCreatedAt(userId, OrderStatus.ACTIVE)
                                        .switchIfEmpty(createNewActiveOrder())
                                        .flatMap(order -> orderItemRepository.findByOrderIdAndProductId(order.getId(), productId)
                                                .flatMap(orderItem -> Mono.error(new IllegalArgumentException(
                                                        "Продукт с ID " + productId + " уже добавлен в заказ"
                                                )))
                                                .switchIfEmpty(Mono.defer(() -> {
                                                    OrderItem orderItem = new OrderItem();
                                                    orderItem.setOrderId(order.getId());
                                                    orderItem.setProductId(productId);
                                                    orderItem.setQuantity(1);
                                                    return orderItemRepository.save(orderItem);
                                                }))
                                                .thenReturn(order)
                                        )
                                        .doOnNext(order -> {
                                            Cache cache = cacheManager.getCache("cart");
                                            if (cache != null) {
                                                String cacheKey = "user:" + userId + ":order:" + order.getId();
                                                cache.evict(cacheKey);
                                            }
                                        })
                                )
                                .then()
                );
    }

    @Transactional
    @Override
    public Mono<Void> updateQuantity(Long productId, Integer delta) {
        return securityUtils.getUserId()
                .switchIfEmpty(Mono.error(new AccessDeniedException("Пользователь не аутентифицирован!")))
                .flatMap(userId ->
                        productRepository.findById(productId)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException(PRODUCT_NOT_FOUND)))
                                .flatMap(product -> orderRepository.findFirstByUserIdAndStatusOrderByCreatedAt(userId, OrderStatus.ACTIVE)
                                        .flatMap(order -> orderItemRepository.findByOrderIdAndProductId(order.getId(), productId)
                                                .flatMap(orderItem -> {
                                                    int newQuantity = orderItem.getQuantity() + delta;
                                                    if (newQuantity == 0) {
                                                        return orderItemRepository.delete(orderItem);
                                                    } else {
                                                        orderItem.setQuantity(newQuantity);
                                                        return orderItemRepository.save(orderItem);
                                                    }
                                                }).thenReturn(order)
                                        )
                                )
                                .doOnNext(order -> {
                                    Cache cache = cacheManager.getCache("cart");
                                    if (cache != null) {
                                        String cacheKey = "user:" + userId + ":order:" + order.getId();
                                        cache.evict(cacheKey);
                                    }
                                })
                                .then()
                );
    }

    @Transactional
    @Override
    public Mono<Void> removeProduct(Long productId) {
        return securityUtils.getUserId()
                .switchIfEmpty(Mono.error(new AccessDeniedException("Пользователь не аутентифицирован!")))
                .flatMap(userId -> orderRepository.findFirstByUserIdAndStatusOrderByCreatedAt(userId, OrderStatus.ACTIVE)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Активный заказ не найден")))
                        .flatMap(order -> productRepository.findById(productId)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Продукт не найден")))
                                .flatMap(product -> orderItemRepository.findByOrderIdAndProductId(order.getId(), productId)
                                        .flatMap(orderItemRepository::delete)
                                        .then()).thenReturn(order))
                        .doOnNext(order -> {
                            Cache cache = cacheManager.getCache("cart");
                            if (cache != null) {
                                String cacheKey = "user:" + userId + ":order:" + order.getId();
                                cache.evict(cacheKey);
                            }
                        })
                        .then()
                );
    }

    @Override
    public Mono<Void> moveCartToCheckout(Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Нет активного заказа. Невозможно оформить заказ")))
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
                .flatMap(order -> orderItemRepository.findAllByOrderId(order.getId(), Sort.by("id").ascending())
                        .collectList()
                        .flatMap(items -> {
                            List<Long> productIds = items.stream().map(OrderItem::getProductId).toList();
                            return getProductMap(productIds)
                                    .map(productMap -> OrderUtil.buildOrderDto(order, orderItemMapper.map(items), productMap));
                        })
                        .flatMap(orderDto -> paymentService.getBalance()
                                .filter(balance -> balance.compareTo(BigDecimal.valueOf(orderDto.getTotalPrice())) >= 0)
                                .switchIfEmpty(Mono.error(new NotEnoughMoneyException("Недостаточно средств")))
                                .thenReturn(orderDto)
                        )
                        .flatMap(orderDto -> paymentService.processPayment(BigDecimal.valueOf(orderDto.getTotalPrice()))
                                .thenReturn(order))
                        .flatMap(orderToUpdate -> {
                            orderToUpdate.setStatus(OrderStatus.PAID);
                            return orderRepository.save(orderToUpdate);
                        })
                )
                .then();
    }

    private Mono<Order> createNewActiveOrder() {
        return securityUtils.getUserId()
                .flatMap(userId -> {
                    Order newOrder = new Order();
                    newOrder.setUserId(userId);
                    newOrder.setStatus(OrderStatus.ACTIVE);
                    newOrder.setCreatedAt(LocalDate.now());
                    return orderRepository.save(newOrder);
                });
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

}
