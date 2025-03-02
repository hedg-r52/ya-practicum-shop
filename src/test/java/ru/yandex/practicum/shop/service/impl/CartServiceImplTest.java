package ru.yandex.practicum.shop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.mapper.OrderMapper;
import ru.yandex.practicum.shop.mapper.OrderMapperImpl;
import ru.yandex.practicum.shop.mapper.ProductMapperImpl;
import ru.yandex.practicum.shop.repository.OrderItemRepository;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.CartService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CartServiceImpl.class, OrderMapper.class, ProductMapperImpl.class})
class CartServiceImplTest {

    @Autowired
    private CartService cartService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    void whenGetCart_ThenReturnLastActiveOrder() {
        var order = prepareActiveOrderWithProduct();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.of(order));

        Order cart = cartService.getCart().orElse(new Order());

        assertEquals(1L, cart.getId());
        assertEquals(OrderStatus.ACTIVE, cart.getStatus());
        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().getFirst().getQuantity());
        assertEquals("Product 1", cart.getItems().getFirst().getProduct().getName());
    }

    @Test
    void whenAddProductWithActive_ThenShouldAdd() {
        var order = prepareActiveOrderWithProduct();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.of(order));
        when(productRepository.findById(2L))
                .thenReturn(Optional.of(getProduct2()));

        cartService.addProduct(2L);

        verify(orderRepository, times(1)).findFirstByStatusOrderByCreatedAtDesc(any());
        verify(productRepository, times(1)).findById(2L);
        verify(orderRepository, times(1)).save(any());
        verify(orderItemRepository, times(1)).save(any());
    }

    @Test
    void whenAddProductAndProductExists_ThenShouldThrowException() {
        var order = prepareActiveOrderWithProduct();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addProduct(1L));

    }

    @Test
    void whenAddTwoItems_ThenQuantityShouldBeGreaterByTwo() {
        var order = prepareActiveOrderWithProduct();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.of(order));

        cartService.updateQuantity(1L, 2);

        Order cart = cartService.getCart().orElse(new Order());
        assertEquals(7, cart.getItems().getFirst().getQuantity());
        verify(orderItemRepository, times(1)).save(any());
    }

    @Test
    void whenSubtractAllQuantity_ThenOrderItemShouldBeRemovedFromOrder() {
        var order = prepareActiveOrderWithProduct();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.of(order));

        cartService.updateQuantity(1L, -5);

        Order cart = cartService.getCart().orElse(new Order());
        assertEquals(0, cart.getItems().size());
        verify(orderItemRepository, times(0)).save(any());
    }

    @Test
    void whenChangeQuantityForNotPresentedProductAtOrder_ThenShouldThrowException() {
        var order = prepareActiveOrderWithProduct();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.updateQuantity(2L, 1));
    }

    @Test
    void whenProductExistsAndRemoveIt_ThenShouldItemsWithoutThisProduct() {
        var order = prepareActiveOrderWithProduct();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.of(order));

        cartService.removeProduct(1L);
        Order cart = cartService.getCart().orElse(new Order());
        assertEquals(0, cart.getItems().size());
    }

    @Test
    void whenNoActiveOrderAndTriesRemoveProduct_ThenShouldThrowException() {
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> cartService.removeProduct(1L));
    }

    @Test
    void whenProductAbsentAndRemoveIt_ThenShouldThrowException() {
        var order = prepareActiveOrderWithProduct();
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.removeProduct(2L));
    }

    @Test
    void whenMoveActiveOrderToCheckout_ThenNewStatusShouldEqualsCheckout() {
        var order = prepareActiveOrderWithProduct();
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.of(order));

        cartService.moveCartToCheckout(1L);

        Order cart = cartService.getCart().orElse(new Order());
        assertEquals(OrderStatus.CHECKOUT, cart.getStatus());
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void whenNoActiveOrder_ThenShouldThrowException() {
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> cartService.moveCartToCheckout(1L));
    }

    @Test
    void whenConfirmPurchaseWithActiveOrder_ThenNewStatusShouldEqualsCheckout() {
        var order = prepareActiveOrderWithProduct();
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.of(order));

        cartService.confirmPurchase(1L);

        Order cart = cartService.getCart().orElse(new Order());
        assertEquals(OrderStatus.PAID, cart.getStatus());
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void whenConfirmPurchaseWithoutActiveOrder_ThenShouldThrowException() {
        when(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> cartService.confirmPurchase(1L));
    }


    private Order prepareActiveOrderWithProduct() {
        var order = Order.builder()
                .id(1L)
                .status(OrderStatus.ACTIVE)
                .createdAt(LocalDate.now())
                .build();
        var product1 = Product.builder()
                .id(1L)
                .image(new Image())
                .name("Product 1")
                .price(100.00f)
                .description("Description of Product1")
                .build();
        var orderItem1 = OrderItem.builder()
                .id(1L)
                .order(order)
                .product(product1)
                .quantity(5)
                .build();
        order.setItems(new ArrayList<>(List.of(orderItem1)));

        return order;
    }

    private Product getProduct2() {
        return Product.builder()
                .id(2L)
                .image(new Image())
                .name("Product 2")
                .price(120.00f)
                .description("Description of Product2")
                .build();
    }
}
