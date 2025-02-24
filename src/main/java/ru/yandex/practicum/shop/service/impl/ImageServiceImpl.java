package ru.yandex.practicum.shop.service.impl;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.shop.entity.Image;
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
    public Optional<Image> getImageById(Long id) {
       return imageRepository.findById(id);
    }
}
