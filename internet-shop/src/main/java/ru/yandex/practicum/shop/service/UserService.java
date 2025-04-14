package ru.yandex.practicum.shop.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.UserDto;
import ru.yandex.practicum.shop.dto.UserResponseDto;

public interface UserService {
    Mono<UserResponseDto> save(UserDto userDto);

    Mono<UserResponseDto> findById(Long id);
}
