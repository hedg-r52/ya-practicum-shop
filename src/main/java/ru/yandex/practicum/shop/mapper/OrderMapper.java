package ru.yandex.practicum.shop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.entity.Order;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(order))")
    OrderDto toOrderDto(Order order);

    Order toOrder(OrderDto orderDto);

    default Float calculateTotalPrice(Order order) {
        var totalPrice = order.getItems().stream()
                .map(item -> item.getQuantity() * item.getProduct().getPrice())
                .reduce(Float::sum)
                .orElse(0.0f);
        return Math.round(totalPrice * 100.0f) / 100.0f;
    }
}
