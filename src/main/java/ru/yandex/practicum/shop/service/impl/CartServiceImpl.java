package ru.yandex.practicum.shop.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    @Override
    public void addProduct(Long productId) {
        Optional<Order> orderOptional = orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE);
        Order order;
        if (orderOptional.isEmpty()) {
            order = Order.builder()
                    .status(OrderStatus.ACTIVE)
                    .createdAt(LocalDate.now())
                    .build();
        } else {
            order = orderOptional.get();
            var productExists = order.getItems().stream()
                    .map(OrderItem::getProduct)
                    .map(Product::getId)
                    .anyMatch(id -> id.equals(productId));
            if (productExists) {
                throw new IllegalArgumentException("Продукт с ID " + productId + " уже добавлен в заказ");
            }
        }
        Product product = productRepository.findById(productId).orElseThrow();
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .order(order)
                .quantity(1)
                .build();

        orderRepository.save(order);
        orderItemRepository.save(orderItem);

    }

    @Override
    public Integer getProductQuantity(Long productId) {
        return 0;
    }

    @Override
    public void updateQuantity(Long productId, Integer quantity) {

    }

    @Override
    public void removeProduct(Long productId) {

    }
}
