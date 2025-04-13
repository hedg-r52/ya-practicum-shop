package ru.yandex.practicum.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.service.PaymentService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/deposit/{orderId}")
    public Mono<String> deposit(@PathVariable Long orderId) {
        return paymentService.depositPayment(BigDecimal.valueOf(5000))
                .thenReturn("redirect:/cart/checkout/" + orderId);
    }

}
