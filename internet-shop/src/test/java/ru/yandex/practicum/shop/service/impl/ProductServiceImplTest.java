package ru.yandex.practicum.shop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.shop.config.TestCacheConfig;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.mapper.ProductMapper;
import ru.yandex.practicum.shop.repository.ImageRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.PaymentService;
import ru.yandex.practicum.shop.service.ProductService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ProductServiceImpl.class, ProductMapper.class})
@Import(TestCacheConfig.class)
@ActiveProfiles("test")
class ProductServiceImplTest {

    @Autowired
    private ProductService productService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private ImageRepository imageRepository;

    @MockitoBean
    private ProductMapper productMapper;

    @Test
    void whenFindAll_ThenShouldReturnPageableResponse() {
        Pageable pageable = PageRequest.of(0, 2);

        Product product1 = Product.builder()
                .id(1L)
                .name("Product 1")
                .build();
        Product product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .build();

        List<Product> products = List.of(product1, product2);

        ProductDto productDto1 = new ProductDto();
        productDto1.setId(1L);
        productDto1.setName("Product 1");
        ProductDto productDto2 = new ProductDto();
        productDto2.setId(2L);
        productDto2.setName("Product 2");

        when(productRepository.findAllBy(pageable))
                .thenReturn(Flux.fromIterable(products));
        when(productRepository.count())
                .thenReturn(Mono.just(2L));
        when(productMapper.toProductDto(product1)).thenReturn(productDto1);
        when(productMapper.toProductDto(product2)).thenReturn(productDto2);

        StepVerifier.create(productService.findAll(pageable))
                .assertNext(page -> {
                    assertEquals(2, page.getContent().size());
                    assertEquals(productDto1, page.getContent().get(0));
                    assertEquals(productDto2, page.getContent().get(1));
                })
                .verifyComplete();
    }

    @Test
    void whenFindAllByNameContainingIgnoreCase_ThenShouldReturnPageableResponse() {
        Pageable pageable = PageRequest.of(0, 2);

        Product product1 = Product.builder()
                .id(1L)
                .name("Product 1")
                .build();
        Product product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .build();

        List<Product> products = List.of(product1, product2);

        ProductDto productDto1 = new ProductDto();
        productDto1.setId(1L);
        productDto1.setName("Product 1");
        ProductDto productDto2 = new ProductDto();
        productDto2.setId(2L);
        productDto2.setName("Product 2");

        when(productRepository.findAllByNameContainingIgnoreCase("Product", pageable))
                .thenReturn(Flux.fromIterable(products));
        when(productRepository.count())
                .thenReturn(Mono.just(2L));
        when(productMapper.toProductDto(product1)).thenReturn(productDto1);
        when(productMapper.toProductDto(product2)).thenReturn(productDto2);

        StepVerifier.create(productService.findAllByNameContainingIgnoreCase("Product", pageable))
                .assertNext(page -> {
                    assertEquals(2, page.getContent().size());
                    assertEquals(productDto1, page.getContent().get(0));
                    assertEquals(productDto2, page.getContent().get(1));
                })
                .verifyComplete();

        verify(productRepository, times(1)).findAllByNameContainingIgnoreCase(anyString(), any());
        verify(productMapper, times(2)).toProductDto(any());
    }

    @Test
    void whenGetProductByIdAndItExists_ThenReturnProduct() {
        Product product = Product.builder()
                .id(1L)
                .name("Product 1")
                .price(100.00f)
                .description("Description of Product 1")
                .build();

        ProductDto productDto = new ProductDto(
                1L,
                "Product 1",
                100.00f,
                "Description of Product 1",
                false,
                0
        );

        when(productMapper.toProductDto(product)).thenReturn(productDto);
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));

        StepVerifier.create(productService.getProductById(1L))
                .assertNext(foundProduct -> {
                    assertEquals(1L, foundProduct.getId());
                    assertEquals("Product 1", foundProduct.getName());
                    assertEquals(100.0f, foundProduct.getPrice());
                })
                .verifyComplete();

        verify(productRepository, times(1)).findById(anyLong());
        verify(productMapper, times(1)).toProductDto(any());
    }

    @Test
    void whenSaveProductWithImage_ThenShouldSaveBoth() {
        Product product = Product.builder()
                .id(1L)
                .name("Product 1")
                .price(100.00f)
                .description("Description of Product 1")
                .build();

        ProductDto productDto = new ProductDto(
                1L,
                "Product 1",
                100.00f,
                "Description of Product 1",
                false,
                0
        );

        when(productMapper.toProduct(productDto)).thenReturn(product);

        String fileContent = "file content";
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(fileContent.getBytes(StandardCharsets.UTF_8));

        FilePart filePart = new FilePart() {
            @Override
            public String filename() {
                return "image.png";
            }

            @Override
            public Mono<Void> transferTo(Path dest) {
                return null;
            }

            @Override
            public Flux<DataBuffer> content() {
                return Flux.just(dataBuffer);
            }

            @Override
            public String name() {
                return "file";
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.EMPTY;
            }

        };

        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product));

        when(imageRepository.save(any(Image.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        StepVerifier.create(productService.saveProductWithImage(productDto, filePart))
                .verifyComplete();

        verify(imageRepository, times(1)).save(any());
        verify(productMapper, times(1)).toProduct(any());
        verify(productRepository, times(1)).save(any());
    }
}
