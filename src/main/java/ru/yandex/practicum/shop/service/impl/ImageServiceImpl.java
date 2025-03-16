package ru.yandex.practicum.shop.service.impl;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.exception.ImageNotFoundException;
import ru.yandex.practicum.shop.repository.ImageRepository;
import ru.yandex.practicum.shop.service.ImageService;

import java.util.Optional;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Mono<Image> getImageById(Long id) {
       return imageRepository.findById(id)
               .switchIfEmpty(Mono.error(new ImageNotFoundException("Image not found with id: " + id)));
    }
}
