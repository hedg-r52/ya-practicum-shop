package ru.yandex.practicum.shop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.mapper.OrderItemMapperImpl;
import ru.yandex.practicum.shop.mapper.OrderMapper;
import ru.yandex.practicum.shop.repository.OrderItemRepository;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.OrderService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {OrderServiceImpl.class, OrderMapper.class, OrderItemMapperImpl.class})
class OrderServiceImplTest {

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderMapper orderMapper;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    void whenFindLastActiveOrder_whenGetActiveOrder() {
        Order order = Order.builder().id(1L).build();

        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(orderItemRepository.findAllByOrderId(eq(order.getId()), any(Sort.class)))
                .thenReturn(Flux.just(getOrderItem()));
        when(productRepository.findAllById(List.of(1L)))
                .thenReturn(Flux.just(getProduct1()));

        StepVerifier.create(orderService.findLastActiveOrder())
                .assertNext(orderDto -> assertEquals(1L, order.getId()))
                .verifyComplete();

        verify(orderRepository, times(1)).findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE);
    }

    @Test
    void whenFindByIdAndStatus_whenGetActiveOrder() {
        Order order = Order.builder().id(1L).build();

        when(orderRepository.findByIdAndStatus(1L, OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(orderItemRepository.findAllByOrderId(eq(order.getId()), any(Sort.class)))
                .thenReturn(Flux.just(getOrderItem()));
        when(productRepository.findAllById(List.of(1L)))
                .thenReturn(Flux.just(getProduct1()));

        StepVerifier.create(orderService.findByIdAndStatus(1L, OrderStatus.ACTIVE))
                .assertNext(orderDto -> assertEquals(1L, order.getId()))
                .verifyComplete();

        verify(orderRepository, times(1)).findByIdAndStatus(any(), any());
    }

    @Test
    void whenFindAll_whenGetActiveOrder() {
        Pageable pageable = PageRequest.of(0, 2);

        Order order1 = Order.builder().id(1L).build();
        Order order2 = Order.builder().id(2L).build();

        OrderItem oi1 = getOrderItem1();
        OrderItem oi2 = getOrderItem2();

        when(orderRepository.findAllBy(pageable))
                .thenReturn(Flux.just(order1, order2));
        when(orderRepository.count())
                .thenReturn(Mono.just(2L));
        when(orderItemRepository.findAllByOrderIds(List.of(1L, 2L)))
                .thenReturn(Flux.just(oi1, oi2));
        when(productRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(Flux.just(getProduct1(), getProduct2()));

        StepVerifier.create(orderService.findAll(pageable))
                .assertNext(page -> {
                    assertEquals(2, page.getContent().size());
                    assertEquals(1L, page.getContent().get(0).getId());
                    assertEquals(2L, page.getContent().get(1).getId());
                })
                .verifyComplete();

        verify(orderRepository, times(1)).findAllBy(pageable);
    }

    private OrderItem getOrderItem1() {
        return OrderItem.builder()
                .id(1L)
                .orderId(1L)
                .productId(1L)
                .quantity(5)
                .build();
    }

    private OrderItem getOrderItem2() {
        return OrderItem.builder()
                .id(2L)
                .orderId(2L)
                .productId(2L)
                .quantity(5)
                .build();
    }

    private Product getProduct1() {
        return Product.builder()
                .id(1L)
                .name("Product 1")
                .price(100.00f)
                .description("Description of Product1")
                .build();
    }

    private Product getProduct2() {
        return Product.builder()
                .id(2L)
                .name("Product 2")
                .price(120.00f)
                .description("Description of Product2")
                .build();
    }

    private OrderItem getOrderItem() {
        return OrderItem.builder()
                .id(1L)
                .orderId(1L)
                .productId(1L)
                .quantity(5)
                .build();
    }
}
