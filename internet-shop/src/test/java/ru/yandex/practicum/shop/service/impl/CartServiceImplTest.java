package ru.yandex.practicum.shop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.shop.config.TestCacheConfig;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.exception.ResourceNotFoundException;
import ru.yandex.practicum.shop.mapper.OrderItemMapperImpl;
import ru.yandex.practicum.shop.mapper.OrderMapperImpl;
import ru.yandex.practicum.shop.mapper.ProductMapperImpl;
import ru.yandex.practicum.shop.repository.OrderItemRepository;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.CartService;
import ru.yandex.practicum.shop.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CartServiceImpl.class, OrderMapperImpl.class, OrderItemMapperImpl.class, ProductMapperImpl.class})
@Import(TestCacheConfig.class)
@ActiveProfiles("test")
class CartServiceImplTest {

    @Autowired
    private CartService cartService;

    @MockitoBean
    PaymentService paymentService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    void whenGetCart_ThenReturnLastActiveOrder() {
        Order order = getOrder();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));

        when(orderItemRepository.findAllByOrderId(eq(order.getId()), any(Sort.class)))
                .thenReturn(Flux.just(getOrderItem()));

        when(productRepository.findAllById(List.of(1L)))
                .thenReturn(Flux.just(getProduct1()));

        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> {
                    assertEquals(1L, cart.getId());
                    assertEquals(OrderStatus.ACTIVE, cart.getStatus());
                    assertEquals(1, cart.getOrderItems().size());
                    assertEquals(5, cart.getOrderItems().getFirst().getQuantity());
                    assertEquals("Product 1", cart.getOrderItems().getFirst().getProduct().getName());
                })
                .verifyComplete();

    }

    @Test
    void whenAddProductWithActive_ThenShouldAdd() {
        Order order = getOrder();
        Product product = getProduct2();

        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(productRepository.findById(2L))
                .thenReturn(Mono.just(product));
        when(orderItemRepository.findByOrderIdAndProductId(order.getId(), 2L))
                .thenReturn(Mono.empty());
        when(orderItemRepository.save(any(OrderItem.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.addProduct(2L))
                .verifyComplete();

        verify(orderRepository, times(1)).findFirstByStatusOrderByCreatedAtDesc(any());
        verify(productRepository, times(1)).findById(2L);
        verify(orderRepository, times(1)).save(any());
        verify(orderItemRepository, times(1)).save(any());
    }

    @Test
    void whenAddProductAndProductExists_ThenShouldThrowException() {
        Order order = getOrder();
        Product product = getProduct1();

        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(product));
        when(orderItemRepository.findByOrderIdAndProductId(order.getId(), 1L))
                .thenReturn(Mono.just(new OrderItem()));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.addProduct(1L))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Продукт с ID 1 уже добавлен в заказ"))
                .verify();

    }

    @Test
    void whenAddTwoItems_ThenQuantityShouldBeGreaterByTwo() {
        var order = getOrder();
        var orderItem = getOrderItem();
        var product = getProduct1();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(product));
        when(orderItemRepository.findByOrderIdAndProductId(order.getId(), 1L))
                .thenReturn(Mono.just(orderItem));
        when(orderItemRepository.findAllByOrderId(eq(order.getId()), any(Sort.class)))
                .thenReturn(Flux.just(orderItem));
        when(productRepository.findAllById(List.of(1L)))
                .thenReturn(Flux.just(product));
        when(orderItemRepository.save(any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.updateQuantity(1L, 2))
                .verifyComplete();

        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> {
                    assertNotNull(cart);
                    assertFalse(cart.getOrderItems().isEmpty());
                    assertEquals(7, cart.getOrderItems().getFirst().getQuantity());
                })
                .verifyComplete();

        verify(orderItemRepository, times(1)).save(any());
    }

    @Test
    void whenSubtractAllQuantity_ThenOrderItemShouldBeRemovedFromOrder() {
        var order = getOrder();
        var orderItem = getOrderItem();
        var product = getProduct1();

        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(product));
        when(orderItemRepository.findByOrderIdAndProductId(order.getId(), 1L))
                .thenReturn(Mono.just(orderItem));
        when(orderItemRepository.findAllByOrderId(eq(order.getId()), any(Sort.class)))
                .thenReturn(Flux.just(orderItem));
        when(productRepository.findAllById(List.of(1L)))
                .thenReturn(Flux.just(product));
        when(orderItemRepository.delete(any())).thenReturn(Mono.empty());
        when(orderItemRepository.save(any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.updateQuantity(1L, -5))
                .verifyComplete();

        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> {
                    assertNotNull(cart);
                    assertFalse(cart.getOrderItems().isEmpty());
                })
                .verifyComplete();

        verify(orderItemRepository, times(1)).delete(any());
        verify(orderItemRepository, times(0)).save(any());
    }

    @Test
    void whenChangeQuantityForNotPresentedProductAtOrder_ThenShouldThrowException() {
        var order = getOrder();
        var orderItem = getOrderItem();

        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(productRepository.findById(2L))
                .thenReturn(Mono.empty());
        when(orderItemRepository.findByOrderIdAndProductId(order.getId(), 2L))
                .thenReturn(Mono.just(orderItem));

        StepVerifier.create(cartService.updateQuantity(2L, 1))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void whenProductExistsAndRemoveIt_ThenShouldItemsWithoutThisProduct() {
        var order = getOrder();
        var product = getProduct1();
        var orderItem = getOrderItem();

        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(product));
        when(productRepository.findAllById(List.of(1L)))
                .thenReturn(Flux.just(product));
        when(orderItemRepository.findByOrderIdAndProductId(order.getId(), 1L))
                .thenReturn(Mono.just(orderItem));
        when(orderItemRepository.findAllByOrderId(eq(order.getId()), any(Sort.class)))
                .thenReturn(Flux.empty());
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(orderItemRepository.save(any(OrderItem.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(orderItemRepository.delete(any())).thenReturn(Mono.empty());

        StepVerifier.create(cartService.removeProduct(1L))
                .verifyComplete();

        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> assertTrue(cart.getOrderItems().isEmpty()))
                .verifyComplete();
    }

    @Test
    void whenNoActiveOrderAndTriesRemoveProduct_ThenShouldThrowException() {
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.empty());
        when(productRepository.findById(1L))
                .thenReturn(Mono.empty());

        StepVerifier.create(cartService.removeProduct(1L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void whenProductAbsentAndRemoveIt_ThenShouldThrowException() {
        var order = getOrder();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(productRepository.findById(2L))
                .thenReturn(Mono.empty());

        StepVerifier.create(cartService.removeProduct(2L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void whenMoveActiveOrderToCheckout_ThenNewStatusShouldEqualsCheckout() {
        var order = getOrder();
        var product = getProduct1();

        when(orderRepository.findById(1L))
                .thenReturn(Mono.just(order));
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(orderItemRepository.findAllByOrderId(eq(order.getId()), any(Sort.class)))
                .thenReturn(Flux.just(getOrderItem()));
        when(productRepository.findAllById(List.of(1L)))
                .thenReturn(Flux.just(product));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.moveCartToCheckout(1L))
                .verifyComplete();

        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> {
                    assertNotNull(cart);
                    assertEquals(OrderStatus.CHECKOUT, cart.getStatus());
                })
                .verifyComplete();

        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void whenNoActiveOrder_ThenShouldThrowException() {
        when(orderRepository.findById(1L))
                .thenReturn(Mono.empty());
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.empty());
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.moveCartToCheckout(1L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void whenConfirmPurchaseWithActiveOrder_ThenNewStatusShouldEqualsCheckout() {
        var order = getOrder();
        var product = getProduct1();

        when(orderRepository.findById(1L))
                .thenReturn(Mono.just(order));
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.just(order));
        when(orderItemRepository.findAllByOrderId(eq(order.getId()), any(Sort.class)))
                .thenReturn(Flux.just(getOrderItem()));
        when(productRepository.findAllById(List.of(1L)))
                .thenReturn(Flux.just(product));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(paymentService.getBalance())
                .thenReturn(Mono.just(BigDecimal.valueOf(1000)));
        when(paymentService.processPayment(any(BigDecimal.class)))
                .thenReturn(Mono.just(BigDecimal.valueOf(500)));

        StepVerifier.create(cartService.confirmPurchase(1L))
                .verifyComplete();

        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> {
                    assertNotNull(cart);
                    assertEquals(OrderStatus.PAID, cart.getStatus());
                })
                .verifyComplete();

        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void whenConfirmPurchaseWithoutActiveOrder_ThenShouldThrowException() {
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Mono.empty());
        when(orderRepository.findById(1L))
                .thenReturn(Mono.empty());
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.confirmPurchase(1L))
                .expectError(IllegalArgumentException.class)
                .verify();
    }


    private Order getOrder() {
        return Order.builder()
                .id(1L)
                .status(OrderStatus.ACTIVE)
                .createdAt(LocalDate.now())
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
}
