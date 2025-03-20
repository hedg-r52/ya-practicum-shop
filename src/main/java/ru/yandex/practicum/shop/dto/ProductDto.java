package ru.yandex.practicum.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    Long id;
    String name;
    Float price;
    String description;
    boolean inCart;
    Integer quantity;
}
