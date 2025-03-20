package ru.yandex.practicum.shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.mapper.ProductMapper;
import ru.yandex.practicum.shop.repository.ImageRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.ProductService;

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
    public Mono<Void> saveProductWithImage(ProductDto productDto, FilePart file) {
        return DataBufferUtils.join(file.content()) // Собираем весь файл в один DataBuffer
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

}
