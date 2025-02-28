package ru.yandex.practicum.shop.util;

import ru.yandex.practicum.shop.entity.Order;

public class OrderUtil {

    private OrderUtil() {
    }

    public static Float getTotal(Order order) {
        return order.getItems().stream()
                .map(oi -> oi.getQuantity() * oi.getProduct().getPrice())
                .reduce(Float::sum)
                .orElse(0.0f);
    }

}
