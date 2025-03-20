package ru.yandex.practicum.shop.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.entity.Image;

public interface ImageService {

    Mono<Image> getImageById(Long id);
}
