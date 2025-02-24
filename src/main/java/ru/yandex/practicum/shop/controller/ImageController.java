package ru.yandex.practicum.shop.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.shop.service.ImageService;

@Controller
@RequestMapping("/shop/image")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        return imageService.getImageById(id)
                .map(image -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"image.jpg\"")
                        .contentType(MediaType.IMAGE_JPEG)  // Можно менять на PNG, GIF и т.д.
                        .body(image.getImageData()))
                .orElse(ResponseEntity.notFound().build());
    }
}
