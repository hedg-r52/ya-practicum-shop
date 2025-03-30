package ru.yandex.practicum.shop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.exception.ImageNotFoundException;
import ru.yandex.practicum.shop.exception.NotEnoughMoneyException;
import ru.yandex.practicum.shop.exception.ResourceNotFoundException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_ATTR = "error";
    private static final String CODE_FIELD = "code";
    private static final String MESSAGE_FIELD = "message";

    @ExceptionHandler({NotEnoughMoneyException.class})
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public Mono<String> handleNotEnoughMoneyException(Exception e, Model model) {
        model.addAttribute(ERROR_ATTR, Map.of(
                CODE_FIELD, HttpStatus.PAYMENT_REQUIRED,
                MESSAGE_FIELD, e.getMessage()
        ));
        return Mono.just("sww");
    }

    @ExceptionHandler({ImageNotFoundException.class, ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleNotFoundException(Exception e, Model model) {
        model.addAttribute(ERROR_ATTR, Map.of(
                CODE_FIELD, HttpStatus.NOT_FOUND,
                MESSAGE_FIELD, e.getMessage()
        ));
        return Mono.just("sww");
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<String> handleException(Exception e, Model model) {
        model.addAttribute(ERROR_ATTR, Map.of(
                CODE_FIELD, HttpStatus.INTERNAL_SERVER_ERROR,
                MESSAGE_FIELD, e.getMessage()
        ));
        return Mono.just("sww");
    }
}
