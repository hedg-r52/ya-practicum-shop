package ru.yandex.practicum.shop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.mapper.ProductMapper;
import ru.yandex.practicum.shop.repository.ImageRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.ProductService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ProductServiceImpl.class, ProductMapper.class})
class ProductServiceImplTest {

    @Autowired
    private ProductService productService;

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
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        ProductDto productDto1 = new ProductDto();
        productDto1.setId(1L);
        productDto1.setName("Product 1");
        ProductDto productDto2 = new ProductDto();
        productDto2.setId(2L);
        productDto2.setName("Product 2");

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        when(productMapper.toProductDto(product1)).thenReturn(productDto1);
        when(productMapper.toProductDto(product2)).thenReturn(productDto2);


        Page<ProductDto> result = productService.findAll(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(productDto1, result.getContent().get(0));
        assertEquals(productDto2, result.getContent().get(1));
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
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        ProductDto productDto1 = new ProductDto();
        productDto1.setId(1L);
        productDto1.setName("Product 1");
        ProductDto productDto2 = new ProductDto();
        productDto1.setId(2L);
        productDto1.setName("Product 2");

        when(productRepository.findAllByNameContainingIgnoreCase("Product", pageable))
                .thenReturn(productPage);
        when(productMapper.toProductDto(product1)).thenReturn(productDto1);
        when(productMapper.toProductDto(product2)).thenReturn(productDto2);

        Page<ProductDto> result = productService.findAllByNameContainingIgnoreCase(
                "Product",
                PageRequest.of(0, 2)
        );

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(productDto1, result.getContent().get(0));
        assertEquals(productDto2, result.getContent().get(1));
        verify(productRepository, times(1)).findAllByNameContainingIgnoreCase(anyString(), any());
        verify(productMapper, times(2)).toProductDto(any());
    }

    @Test
    void whenGetProductByIdAndItExists_ThenReturnProduct() {
        Product product = Product.builder()
                .id(1L)
                .image(new Image())
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
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        Optional<ProductDto> productDtoOptional = productService.getProductById(1L);

        assertTrue(productDtoOptional.isPresent());
        var foundProduct = productDtoOptional.get();
        assertEquals(1L, foundProduct.getId());
        assertEquals("Product 1", foundProduct.getName());
        assertEquals(100.0f, foundProduct.getPrice());
        verify(productRepository, times(1)).findById(anyLong());
        verify(productMapper, times(1)).toProductDto(any());
    }

    @Test
    void whenSaveProductWithImage_ThenShouldSaveBoth() {
        Product product = Product.builder()
                .id(1L)
                .image(new Image())
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

        productService.saveProductWithImage(productDto, new MockMultipartFile("image", (byte[]) null));

        verify(imageRepository, times(1)).save(any());
        verify(productMapper, times(1)).toProduct(any());
        verify(productRepository, times(1)).save(any());
    }
}
