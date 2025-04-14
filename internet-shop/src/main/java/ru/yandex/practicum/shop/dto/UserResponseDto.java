package ru.yandex.practicum.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String username;
    private LocalDate createdAt;
    private LocalDate modifiedAt;
}
