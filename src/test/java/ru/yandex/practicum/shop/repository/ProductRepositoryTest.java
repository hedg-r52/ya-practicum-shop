package ru.yandex.practicum.shop.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.yandex.practicum.shop.entity.Product;

@SpringBootTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll().block();
    }

    @Test
    void whenFindAllByNameContainingIgnoreCase_ThenShouldReturnResult() {
        productRepository.save(
                Product.builder()
                        .name("Name")
                        .description("Description")
                        .price(100.00f)
                        .build()
        ).block();

        Flux<Product> productsFlux = productRepository.findAllByNameContainingIgnoreCase(
                "Name",
                PageRequest.of(0, 2)
        );

        StepVerifier.create(productsFlux)
                .expectNextCount(1)  // Проверяем, что один продукт был найден
                .verifyComplete();
    }
}