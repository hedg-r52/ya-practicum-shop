package ru.yandex.practicum.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.service.ImageService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(ImageController.class)
class ImageControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    ImageService imageService;

    @Test
    void whenGetImage_shouldReturnByteArray() throws Exception {
        when(imageService.getImageById(1L))
                .thenReturn(getTestImage());

        webTestClient.get()
                .uri(builder ->
                        builder
                                .path("/shop/image/{id}")
                                .build(1L))
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG);

        verify(imageService, times(1)).getImageById(1L);

    }

    private Mono<Image> getTestImage() throws IOException {
        // Создаем маленькое изображение 10x10 пикселей
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 10, 10);
        g.dispose();

        // Конвертируем изображение в массив байтов
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);

        Image image = new Image();
        image.setImageData(baos.toByteArray());

        return Mono.just(image);
    }
}