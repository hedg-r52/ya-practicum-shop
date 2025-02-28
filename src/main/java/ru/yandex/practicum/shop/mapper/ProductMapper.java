package ru.yandex.practicum.shop.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDto toProductDto(Product product);

    Product toProduct(ProductDto productDto);
}
