package ru.yandex.practicum.shop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.shop.entity.Image;

@Repository
public interface ImageRepository extends R2dbcRepository<Image, Long> {
}
