package ru.yandex.practicum.shop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.shop.dto.ProductDto;

public interface ProductService {

    Page<ProductDto> findAll(Pageable pageable);

}
