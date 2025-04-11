package ru.yandex.practicum.shop.util;

import org.mapstruct.Named;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderUtils {

    private final PasswordEncoder passwordEncoder;

    public PasswordEncoderUtils(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Named("encodePassword")
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
