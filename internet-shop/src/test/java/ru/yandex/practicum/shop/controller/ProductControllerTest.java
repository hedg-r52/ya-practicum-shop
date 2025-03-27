package ru.yandex.practicum.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.dto.OrderItemDto;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.ProductService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    OrderService orderService;

    @Test
    void whenGetProductList_shouldGetProductList() {
        Pageable pageable = PageRequest.of(0, 2);
        var products = getProducts();
        Page<ProductDto> productPage = new PageImpl<>(products, pageable, products.size());

        when(productService.findAll(any(Pageable.class)))
                .thenReturn(Mono.just(productPage));
        when(orderService.findLastActiveOrder())
                .thenReturn(Mono.just(prepareOrder(OrderStatus.ACTIVE)));

        webTestClient.get()
                .uri("/shop/product")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<div id=\"products\" class=\"grid-view\">"));
                });

        verify(productService, times(1)).findAll(any(Pageable.class));
        verify(orderService, times(1)).findLastActiveOrder();
    }

    @Test
    void whenGetProductListFiltered_shouldGetProductList() {
        Pageable pageable = PageRequest.of(0, 2);
        var products = getProducts();
        Page<ProductDto> productPage = new PageImpl<>(products, pageable, products.size());

        when(productService.findAllByNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(Mono.just(productPage));
        when(orderService.findLastActiveOrder())
                .thenReturn(Mono.just(prepareOrder(OrderStatus.ACTIVE)));

        webTestClient.get()
                .uri(builder ->
                        builder
                                .path("/shop/product")
                                .queryParam("search", "Product")
                                .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<div id=\"products\" class=\"grid-view\">"));
                });

        verify(productService, times(1)).findAllByNameContainingIgnoreCase(anyString(), any(Pageable.class));
        verify(orderService, times(1)).findLastActiveOrder();
    }

    @Test
    void whenGetProductCard_shouldGetProduct() {
        var productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setName("Product 1");
        productDto.setDescription("Description of Product 1");

        when(productService.getProductById(anyLong()))
                .thenReturn(Mono.just(productDto));
        when(orderService.findLastActiveOrder())
                .thenReturn(Mono.just(prepareOrder(OrderStatus.ACTIVE)));

        webTestClient.get()
                .uri("/shop/product/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<div class=\"product-wrapper\">"));
                });

        verify(productService, times(1)).getProductById(anyLong());
        verify(orderService, times(1)).findLastActiveOrder();
    }

    @Test
    void whenGetAddProduct_shouldReceiveNewProductView() {
        webTestClient.get()
                .uri("/shop/product/add")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<div class=\"wrapper\">"));
                });
    }

    @Test
    void whenPost() {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("imageFile", new byte[0])
                .filename("image.jpg")
                .contentType(MediaType.IMAGE_JPEG);

        FilePart filePartMock = mock(FilePart.class);
        when(filePartMock.filename()).thenReturn("image.jpg");

        when(productService.saveProductWithImage(any(ProductDto.class), any(FilePart.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/shop/product/add")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/shop/product$"); // Проверяем редирект

        verify(productService, times(1))
                .saveProductWithImage(any(ProductDto.class), any(FilePart.class));

    }

    private List<ProductDto> getProducts() {
        var productDto1 = new ProductDto();
        productDto1.setId(1L);
        productDto1.setName("Product 1");
        productDto1.setDescription("Description of Product 1");

        var productDto2 = new ProductDto();
        productDto2.setId(1L);
        productDto2.setName("Product 1");
        productDto2.setDescription("Description of Product 1");

        return new ArrayList<>(List.of(productDto1, productDto2));
    }

    private OrderDto prepareOrder(OrderStatus status) {
        var product1 = ProductDto.builder()
                .id(1L)
                .name("Product 01")
                .description("Description of Product 01")
                .price(20.0f)
                .build();
        var orderItem1 = OrderItemDto.builder()
                .id(1L)
                .orderId(1L)
                .productId(1L)
                .product(product1)
                .quantity(5)
                .build();
        return OrderDto.builder()
                .id(1L)
                .orderItems(new ArrayList<>(List.of(orderItem1)))
                .totalPrice(100.0f)
                .createdAt(LocalDate.now())
                .status(status)
                .build();
    }

}