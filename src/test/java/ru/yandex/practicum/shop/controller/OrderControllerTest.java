package ru.yandex.practicum.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.service.OrderService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;

    @Test
    void whenGetOrders_shouldGetOrderList() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);

        var order1 = new OrderDto();
        order1.setId(1L);
        order1.setStatus(OrderStatus.ACTIVE);
        var order2 = new OrderDto();
        order2.setId(1L);
        order2.setStatus(OrderStatus.ACTIVE);

        var orders = List.of(order1, order2);

        Page<OrderDto> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderService.findAll(any(Pageable.class))).thenReturn(orderPage);

        mockMvc.perform(get("/order")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(content().string(containsString("order-row")));

        verify(orderService, times(1)).findAll(PageRequest.of(0, 2));
    }

    @Test
    void whenGetSummary_shouldShowSummaryOrderPage() throws Exception {
        var order = Order.builder()
                .id(1L)
                .status(OrderStatus.PAID)
                .build();
        var product = Product.builder()
                .id(1L)
                .price(100.0f)
                .build();
        var orderItem = OrderItem.builder()
                .quantity(5)
                .product(product)
                .order(order)
                .build();
        order.setItems(List.of(orderItem));

        when(orderService.findByIdAndStatus(anyLong(), any(OrderStatus.class)))
                .thenReturn(Optional.of(order));

        mockMvc.perform(get("/order/summary/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("summary"));

        verify(orderService, times(1)).findByIdAndStatus(anyLong(), any(OrderStatus.class));
    }

}
