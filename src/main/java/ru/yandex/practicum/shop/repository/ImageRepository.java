package ru.yandex.practicum.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.shop.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
