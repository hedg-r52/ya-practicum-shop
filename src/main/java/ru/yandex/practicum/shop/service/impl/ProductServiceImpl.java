package ru.yandex.practicum.shop.service.impl;

import org.springframework.core.io.buffer.DataBuffer;
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
    public Mono<Page<ProductDto>> findAll(Pageable pageable) {
        return productRepository.findAllBy(pageable)
                .flatMap(product -> Mono.just(productMapper.toProductDto(product)))
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Page<ProductDto>> findAllByNameContainingIgnoreCase(String searchString, Pageable pageable) {
        return productRepository.findAllByNameContainingIgnoreCase(searchString, pageable)
                .flatMap(product -> Mono.just(productMapper.toProductDto(product)))
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
        return Mono.fromCallable(() -> {
                    try {
                        return file.getInputStream(); // Получаем InputStream из MultipartFile
                    } catch (IOException e) {
                        throw new ImageLoadFileException("Ошибка загрузки файла: " + file.getName());
                    }
                })
                .flatMap(inputStream -> DataBufferUtils.readInputStream(() -> inputStream, new DefaultDataBufferFactory(), 4096)
                        .collectList()  // Собираем все DataBuffer в список
                        .map(dataBuffers -> {
                            // Объединяем все DataBuffer в один массив байт
                            int totalLength = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();
                            byte[] bytes = new byte[totalLength];
                            int offset = 0;
                            for (DataBuffer dataBuffer : dataBuffers) {
                                int length = dataBuffer.readableByteCount();
                                dataBuffer.read(bytes, offset, length);
                                offset += length;
                            }
                            return bytes;
                        }))
                .flatMap(bytes -> {
                    // Сохраняем изображение в базе данных
                    Image image = new Image();
                    image.setImageData(bytes);
                    return imageRepository.save(image);
                })
                .flatMap(savedImage -> {
                    // Сохраняем продукт в базе данных
                    Product product = productMapper.toProduct(productDto);
                    product.setImage(savedImage);
                    return productRepository.save(product);
                })
                .then();
    }
}
