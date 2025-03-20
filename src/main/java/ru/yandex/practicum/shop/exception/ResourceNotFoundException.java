package ru.yandex.practicum.shop.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s с id %d не найден", resourceName, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
