package ru.yandex.practicum.shop.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shop.entity.Product;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @Transactional
    @Test
    void whenFindAllByNameContainingIgnoreCase_ThenShouldReturnResult() {
        productRepository.save(
                Product.builder()
                        .name("Name")
                        .description("Description")
                        .price(100.00f)
                        .image(null)
                        .build()
        );

        Page<Product> products = productRepository.findAllByNameContainingIgnoreCase(
                "Name",
                PageRequest.of(0, 2)
        );
        assertEquals(1L, products.getTotalElements());
    }
}