package ru.yandex.practicum.shop.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.repository.ImageRepository;
import ru.yandex.practicum.shop.service.ImageService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ImageServiceImpl.class})
class ImageServiceImplTest {

    @Autowired
    private ImageService imageService;

    @MockitoBean
    private ImageRepository imageRepository;

    @Test
    void whenGetImageById_ThenInvokeFindByIdForRepository() {
        Image image = new Image();
        image.setId(1L);

        when(imageRepository.findById(1L))
                .thenReturn(Mono.just(image));

        StepVerifier.create(imageService.getImageById(1L))
                .expectNextMatches(returnedImage -> {
                    assertEquals(1L, returnedImage.getId());
                    return true;
                })
                .verifyComplete();

        verify(imageRepository, times(1)).findById(anyLong());
    }

}
