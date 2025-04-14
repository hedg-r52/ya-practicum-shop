package ru.yandex.practicum.shop.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.exception.ResourceNotFoundException;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.util.SecurityUtils;

import java.util.Optional;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private static final int DEFAULT_ORDER_PAGE_SIZE = 20;
    private final SecurityUtils securityUtils;

    public OrderController(OrderService orderService, SecurityUtils securityUtils) {
        this.orderService = orderService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public Mono<String> orders(Model model,
                               @RequestParam("page") Optional<Integer> page,
                               @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(DEFAULT_ORDER_PAGE_SIZE);

        return securityUtils.getUserId()
                .flatMap(userId -> orderService.findAll(userId, PageRequest.of(
                                currentPage - 1,
                                pageSize,
                                Sort.by("created_at").ascending().and(Sort.by("id").ascending())))
                        .doOnNext(orders -> {
                            model.addAttribute("orders", orders);

                            int totalPages = orders.getTotalPages();
                            if (totalPages > 0) {
                                long pageNumbers = IntStream.rangeClosed(1, totalPages).count();
                                model.addAttribute("pageNumbers", pageNumbers);
                            }
                        })
                        .thenReturn("orders"));
    }

    @GetMapping("/summary/{id}")
    public Mono<String> summary(Model model, @PathVariable("id") Long orderId) {
        return securityUtils.getUserId()
                .flatMap(userId ->
                        orderService.findByIdAndStatus(orderId, OrderStatus.PAID)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Заказ с id = " + orderId + " не найден или не завершен")))
                                .flatMap(order -> {
                                    if (!order.getUserId().equals(userId)) {
                                        return Mono.error(new AccessDeniedException("Недостаточно прав для доступа к заказу"));
                                    }

                                    model.addAttribute("order", order);
                                    model.addAttribute("total", String.format("%.2f", order.getTotalPrice()));
                                    return Mono.just("summary");
                                })
                );
    }
}
