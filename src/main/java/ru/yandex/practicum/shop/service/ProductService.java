package ru.yandex.practicum.shop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.ProductDto;


public interface ProductService {

    Mono<Page<ProductDto>> findAll(Pageable pageable);

    Mono<Page<ProductDto>> findAllByNameContainingIgnoreCase(String searchString, Pageable pageable);

    Mono<ProductDto> getProductById(Long id);

    Mono<Void> saveProductWithImage(ProductDto productDto, MultipartFile file);
}
