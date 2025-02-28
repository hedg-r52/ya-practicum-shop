package ru.yandex.practicum.shop.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.mapper.ProductMapper;
import ru.yandex.practicum.shop.repository.ImageRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.ProductService;

import java.io.IOException;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              ImageRepository imageRepository,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Page<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toProductDto);
    }

    @Override
    public Page<ProductDto> findAllByNameContainingIgnoreCase(String searchString, Pageable pageable) {
        return productRepository.findAllByNameContainingIgnoreCase(searchString, pageable).map(productMapper::toProductDto);
    }

    @Override
    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id).map(productMapper::toProductDto);
    }

    @Override
    public void saveProductWithImage(ProductDto productDto, MultipartFile file) {
        try {
            Image image = new Image();
            image.setImageData(file.getBytes());
            Image savedImage = imageRepository.save(image);
            Product product = productMapper.toProduct(productDto);
            product.setImage(savedImage);
            productRepository.save(product);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки файла", e);
        }
    }
}
