package ru.yandex.practicum.shop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.shop.dto.ProductDto;

import java.util.Optional;

public interface ProductService {

    Page<ProductDto> findAll(Pageable pageable);

    Page<ProductDto> findAllByNameContainingIgnoreCase(String searchString, Pageable pageable);

    Optional<ProductDto> getProductById(Long id);

    void saveProductWithImage(ProductDto productDto, MultipartFile file);
}
