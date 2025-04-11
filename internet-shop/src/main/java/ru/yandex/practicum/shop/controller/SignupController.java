package ru.yandex.practicum.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.UserDto;
import ru.yandex.practicum.shop.service.UserService;

@Controller
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignupController {

    private final UserService userService;

    @GetMapping
    public Mono<String> signupForm(Model model) {
        return Mono.just("signup");
    }

    @PostMapping
    public Mono<String> save(@ModelAttribute UserDto userDto) {
        return userService.save(userDto)
                .map(userResponseDto -> "signup");
    }
}
