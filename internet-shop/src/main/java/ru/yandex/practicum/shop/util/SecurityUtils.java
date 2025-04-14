package ru.yandex.practicum.shop.util;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.entity.User;

@Component
public class SecurityUtils {
    public Mono<Long> getUserId() {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .switchIfEmpty(Mono.empty())
                .map(auth -> (User) auth.getPrincipal())
                .map(User::getId);
    }
}
