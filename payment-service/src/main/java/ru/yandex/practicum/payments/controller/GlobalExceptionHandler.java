package ru.yandex.practicum.payments.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payments.exception.NotEnoughMoneyException;
import ru.yandex.practicum.payments.exception.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NotEnoughMoneyException.class})
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public Mono<ErrorResponse> handleBadRequestException(Exception e) {
        return Mono.just(ErrorResponse.builder()
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponse> handleResourceNotFoundException(Exception e) {
        return Mono.just(ErrorResponse.builder()
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleException(Exception e) {
        return Mono.just(ErrorResponse.builder()
                .message(e.getMessage())
                .build());
    }
}
