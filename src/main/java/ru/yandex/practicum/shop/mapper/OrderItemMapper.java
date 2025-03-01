package ru.yandex.practicum.shop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.shop.dto.OrderItemDto;
import ru.yandex.practicum.shop.entity.OrderItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    List<OrderItemDto> map(List<OrderItem> orderItems);

    @Mapping(target = "product.inCart", ignore = true)
    @Mapping(target = "product.quantity", ignore = true)
    OrderItemDto toOrderItemDto(OrderItem orderItem);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product.image", ignore = true)
    OrderItem toOrderItem(OrderItemDto orderItemDto);
}
