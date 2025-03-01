package ru.yandex.practicum.shop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.shop.repository.ImageRepository;
import ru.yandex.practicum.shop.service.ImageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {ImageServiceImpl.class})
class ImageServiceImplTest {

    @Autowired
    private ImageService imageService;

    @MockitoBean
    private ImageRepository imageRepository;

    @Test
    void whenGetImageById_ThenInvokeFindByIdForRepository() {
        imageService.getImageById(1L);
        verify(imageRepository, times(1)).findById(any());
    }

}
