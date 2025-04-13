package ru.yandex.practicum.shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.UserDto;
import ru.yandex.practicum.shop.dto.UserResponseDto;
import ru.yandex.practicum.shop.mapper.UserMapper;
import ru.yandex.practicum.shop.repository.UserRepository;
import ru.yandex.practicum.shop.service.UserService;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<UserResponseDto> save(UserDto userDto) {
        var user = userMapper.map(userDto);
        return userRepository.save(user)
                .map(userMapper::map);
    }

    @Override
    public Mono<UserResponseDto> findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::map);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByLogin(username)
                .map(UserDetails.class::cast);
    }
}
