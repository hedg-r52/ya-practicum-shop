package ru.yandex.practicum.shop.service;

import ru.yandex.practicum.shop.entity.Image;

import java.util.Optional;

public interface ImageService {

    Optional<Image> getImageById(Long id);
}
