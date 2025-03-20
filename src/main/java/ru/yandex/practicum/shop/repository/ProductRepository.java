package ru.yandex.practicum.shop.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.shop.entity.Product;

@Repository
public interface ProductRepository extends R2dbcRepository<Product, Long> {

    Flux<Product> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Flux<Product> findAllBy(Pageable pageable);
}
