package ru.yandex.practicum.shop.util;

import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.dto.OrderItemDto;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Order;

import java.util.List;
import java.util.Map;

public class OrderUtil {

    private OrderUtil() {
    }

    public static OrderDto buildOrderDto(Order order, List<OrderItemDto> items, Map<Long, ProductDto> productMap) {
        List<OrderItemDto> enrichedItems = items.stream()
                .map(item -> {
                    item.setProduct(productMap.get(item.getProductId()));
                    return item;
                })
                .toList();

        return OrderDto.builder()
                .id(order.getId())
                .orderItems(enrichedItems)
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .totalPrice(
                        items.stream()
                                .map(oi -> {
                                    ProductDto productDto = productMap.get(oi.getProductId());
                                    if (productDto == null && productDto.getPrice() == null) {
                                        throw new IllegalStateException("Цена продукта с ID " + oi.getProductId() + " не найдена");
                                    }
                                    var price = (productDto.getPrice() * 100) / 100f;
                                    return oi.getQuantity() * price;
                                })
                                .reduce(0f, Float::sum)
                )
                .build();
    }
}
