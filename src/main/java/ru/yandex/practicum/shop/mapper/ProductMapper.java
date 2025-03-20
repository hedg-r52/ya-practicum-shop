package ru.yandex.practicum.shop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "inCart", ignore = true)
    @Mapping(target = "quantity", ignore = true)
    ProductDto toProductDto(Product product);

    Product toProduct(ProductDto productDto);
}
