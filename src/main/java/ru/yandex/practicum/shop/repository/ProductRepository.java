package ru.yandex.practicum.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.shop.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
