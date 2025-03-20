package ru.yandex.practicum.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.shop.entity.OrderStatus;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private List<OrderItemDto> orderItems;
    private OrderStatus status;
    private LocalDate createdAt;
    private Float totalPrice;
}
