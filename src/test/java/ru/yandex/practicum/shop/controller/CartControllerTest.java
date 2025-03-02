package ru.yandex.practicum.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.service.CartService;
import ru.yandex.practicum.shop.service.OrderService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CartService cartService;

    @MockitoBean
    OrderService orderService;

    @Test
    void whenGetCart_shouldReturnListOfProducts() throws Exception {
        Order order = prepareOrder(OrderStatus.ACTIVE);
        when(cartService.getCart())
                .thenReturn(Optional.of(order));

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("cart"))
                .andExpect(content().string(containsString("cart-row")));
    }

    @Test
    void whenGetCheckoutPage_shouldReturnCheckoutOrder() throws Exception {
        Order order = prepareOrder(OrderStatus.CHECKOUT);
        when(orderService.findByIdAndStatus(1L, OrderStatus.CHECKOUT))
                .thenReturn(Optional.of(order));

        mockMvc.perform(get("/cart/checkout/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("checkout"))
                .andExpect(content().string(containsString("cart-row")));
    }

    @Test
    void whenPostCheckout_shouldChangeOrderStatusToCheckout() throws Exception {
        mockMvc.perform(post("/cart/checkout/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/cart/checkout/1"));

        verify(cartService, times(1)).moveCartToCheckout(1L);
    }

    @Test
    void whenPostPurchase_shouldChangeOrderStatusToPaid() throws Exception {
        mockMvc.perform(post("/cart/purchase/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/order/summary/1"));

        verify(cartService, times(1)).confirmPurchase(1L);
    }

    @Test
    void whenPostAddToCart_shouldAddProductToCart() throws Exception {
        mockMvc.perform(post("/cart/add/{id}", 1L))
                .andExpect(status().isOk());

        verify(cartService, times(1)).addProduct(anyLong());
    }

    @Test
    void whenPostUpdateQuantity_shouldChangeQuantity() throws Exception {
        mockMvc.perform(post("/cart/update/{id}", 1L)
                        .param("change", "3"))
                .andExpect(status().isOk());

        verify(cartService, times(1)).updateQuantity(anyLong(), anyInt());
    }

    @Test
    void whenPostRemoveProductFromCart_shouldRemoveOrderItemFromCart() throws Exception {
        mockMvc.perform(post("/cart/remove/{id}", 1L))
                .andExpect(status().isOk());

        verify(cartService, times(1)).removeProduct(1L);
    }

    private Order prepareOrder(OrderStatus status) {
        var order = Order.builder()
                .id(1L)
                .status(status)
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
}