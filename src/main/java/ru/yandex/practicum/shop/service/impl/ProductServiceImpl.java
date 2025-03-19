package ru.yandex.practicum.shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.exception.ImageLoadFileException;
import ru.yandex.practicum.shop.mapper.ProductMapper;
import ru.yandex.practicum.shop.repository.ImageRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.ProductService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final ProductMapper productMapper;

    @Override
    public Mono<Page<ProductDto>> findAll(Pageable pageable) {
        return productRepository.findAllBy(pageable)
                .map(productMapper::toProductDto)
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Page<ProductDto>> findAllByNameContainingIgnoreCase(String searchString, Pageable pageable) {
        return productRepository.findAllByNameContainingIgnoreCase(searchString, pageable)
                .map(productMapper::toProductDto)
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<ProductDto> getProductById(Long id) {
        return productRepository.findById(id).map(productMapper::toProductDto);
    }

    @Override
    public Mono<Void> saveProductWithImage(ProductDto productDto, MultipartFile file) {
        return DataBufferUtils.join(DataBufferUtils.readInputStream(() -> {
                    try {
                        return file.getInputStream();
                    } catch (IOException e) {
                        throw new ImageLoadFileException("Ошибка загрузки файла: " + file.getName());
                    }
                }, new DefaultDataBufferFactory(), 4096))
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .flatMap(bytes -> {
                    Image image = new Image();
                    image.setImageData(bytes);
                    return imageRepository.save(image);
                })
                .flatMap(savedImage -> {
                    Product product = productMapper.toProduct(productDto);
                    product.setImageId(savedImage.getId());
                    return productRepository.save(product);
                })
                .then();
    }

    public Mono<Map<Long, Float>> getProductPriceMap(List<Long> productIds) {
        return productRepository.findAllById(productIds)
                .collectMap(Product::getId, Product::getPrice);
    }
}
