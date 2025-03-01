package ru.yandex.practicum.shop.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Transactional
    @Rollback
    @Test
    void whenFindFistByStatusOrderByCreatedAtDesc_thenShouldReturnOrder() {
        Product product = Product.builder()
                .name("Name")
                .description("Description")
                .price(100.00f)
                .image(null)
                .build();
        productRepository.save(product);

        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(3)
                .build();
        Order order = Order.builder()
                .status(OrderStatus.ACTIVE)
                .items(List.of(orderItem))
                .createdAt(LocalDate.now())
                .build();
        orderItem.setOrder(order);
        orderRepository.save(order);

        Optional<Order> foundOpt = orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE);

        assertTrue(foundOpt.isPresent());
        Order found = foundOpt.get();
        assertEquals(OrderStatus.ACTIVE, found.getStatus());
        assertEquals(1, found.getItems().size());
    }

    @Transactional
    @Rollback
    @Test
    void whenFindByIdAndStatus_thenShouldReturnOrder() {
        Product product = Product.builder()
                .name("Name")
                .description("Description")
                .price(100.00f)
                .image(null)
                .build();
        productRepository.save(product);

        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(3)
                .build();
        Order order = Order.builder()
                .status(OrderStatus.ACTIVE)
                .items(List.of(orderItem))
                .createdAt(LocalDate.now())
                .build();
        orderItem.setOrder(order);
        orderRepository.save(order);

        Optional<Order> foundOpt = orderRepository.findByIdAndStatus(1L, OrderStatus.ACTIVE);
        assertTrue(foundOpt.isPresent());
        Order found = foundOpt.get();
        assertEquals(OrderStatus.ACTIVE, found.getStatus());
        assertEquals(1L, found.getId());
        assertEquals(1, found.getItems().size());

    }


}
