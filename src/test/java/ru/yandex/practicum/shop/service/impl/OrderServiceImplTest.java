package ru.yandex.practicum.shop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.mapper.OrderMapper;
import ru.yandex.practicum.shop.mapper.OrderMapperImpl;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.service.OrderService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {OrderServiceImpl.class, OrderMapper.class})
class OrderServiceImplTest {

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderMapper orderMapper;

    @Test
    void whenFindLastActiveOrder_whenGetActiveOrder() {
        orderService.findLastActiveOrder();
        verify(orderRepository, times(1)).findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE);
    }

    @Test
    void whenFindByIdAndStatus_whenGetActiveOrder() {
        orderService.findByIdAndStatus(1L, OrderStatus.ACTIVE);
        verify(orderRepository, times(1)).findByIdAndStatus(any(), any());
    }

    @Test
    void whenFindAll_whenGetActiveOrder() {
        Pageable pageable = PageRequest.of(0, 2);

        Order order1 = Order.builder()
                .id(1L)
                .build();
        Order order2 = Order.builder()
                .id(2L)
                .build();

        List<Order> orderList = List.of(order1, order2);
        Page<Order> orderPage = new PageImpl<>(orderList, pageable, orderList.size());

        OrderDto orderDto1 = new OrderDto();
        orderDto1.setId(1L);
        OrderDto orderDto2 = new OrderDto();
        orderDto1.setId(2L);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        when(orderMapper.toOrderDto(order1)).thenReturn(orderDto1);
        when(orderMapper.toOrderDto(order2)).thenReturn(orderDto2);

        Page<OrderDto> result = orderService.findAll(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(orderDto1, result.getContent().get(0));
        assertEquals(orderDto2, result.getContent().get(1));

        verify(orderRepository, times(1)).findAll(pageable);

        verify(orderMapper, times(1)).toOrderDto(order1);
        verify(orderMapper, times(1)).toOrderDto(order2);
    }
}
