package ru.yandex.practicum.shop.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.util.OrderUtil;

import java.util.Optional;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private static final int DEFAULT_ORDER_PAGE_SIZE = 20;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String orders(Model model,
                         @RequestParam("page") Optional<Integer> page,
                         @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(DEFAULT_ORDER_PAGE_SIZE);
        Page<OrderDto> orders = orderService.findAll(PageRequest.of(currentPage - 1, pageSize));
        model.addAttribute("orders", orders);

        int totalPages = orders.getTotalPages();
        if (totalPages > 0) {
            long pageNumbers = IntStream.rangeClosed(1, totalPages).count();
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "orders";
    }

    @GetMapping("/summary/{id}")
    public String summary(Model model, @PathVariable("id") Long orderId) {
        var order = orderService.findByIdAndStatus(orderId, OrderStatus.PAID).orElseThrow(
                () -> new IllegalArgumentException("Заказ с id = " + orderId + " не найден или не завершен")
        );
        var total = OrderUtil.getTotal(order);
        model.addAttribute("order", order);
        model.addAttribute("total", String.format("%.2f", total));
        return "summary";
    }
}
