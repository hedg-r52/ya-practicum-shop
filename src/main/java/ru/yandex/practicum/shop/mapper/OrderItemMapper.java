package ru.yandex.practicum.shop.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.shop.dto.OrderItemDto;
import ru.yandex.practicum.shop.entity.OrderItem;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    List<OrderItemDto> map(List<OrderItem> orderItems);

    OrderItemDto toOrderItemDto(OrderItem orderItem);

    OrderItem toOrderItem(OrderItemDto orderItemDto);
}
