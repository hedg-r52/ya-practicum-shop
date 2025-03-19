package ru.yandex.practicum.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.service.CartService;
import ru.yandex.practicum.shop.service.OrderService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private static final String SUCCESS = "success";
    private final OrderService orderService;

    public CartController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping
    public Mono<String> cart(Model model) {
        return cartService.getCart()
                .doOnNext(cart -> {
                    var cartEmpty = cart == null || cart.getOrderItems().isEmpty();
                    model.addAttribute("empty", cartEmpty);
                    if (!cartEmpty) {
                        model.addAttribute("total", String.format("%.2f", cart.getTotalPrice()));
                    }
                    model.addAttribute("cart", cartEmpty ? new Order() : cart);
                })
                .thenReturn("cart");
    }

    @GetMapping("/checkout/{orderId}")
    public Mono<String> checkoutPage(Model model, @PathVariable Long orderId) {
        return orderService.findByIdAndStatus(orderId, OrderStatus.CHECKOUT)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Заказ с id = " + orderId + " не найден")))
                .doOnNext(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("total", String.format("%.2f", order.getTotalPrice()));
                })
                .thenReturn("checkout");
    }

    @PostMapping("/checkout/{orderId}")
    public Mono<String> moveOrderToCheckoutStage(@PathVariable Long orderId) {
        return cartService.moveCartToCheckout(orderId)
                .thenReturn("redirect:/cart/checkout/" + orderId);
    }

    @PostMapping("/purchase/{orderId}")
    public Mono<String> confirmPurchase(@PathVariable Long orderId) {
        return cartService.confirmPurchase(orderId)
                .thenReturn("redirect:/order/summary/" + orderId);
    }

    @PostMapping("/add/{productId}")
    public Mono<ResponseEntity<Map<String, Object>>> addToCart(@PathVariable Long productId) {
        return cartService.addProduct(productId)
                .thenReturn(createSuccessResponse());
    }

    @PostMapping("/update/{productId}")
    public Mono<ResponseEntity<Map<String, Object>>> updateQuantity(@PathVariable Long productId, @RequestParam("change") int quantity) {
        return cartService.updateQuantity(productId, quantity)
                .thenReturn(createSuccessResponse());
    }

    @PostMapping("/remove/{productId}")
    public Mono<ResponseEntity<Map<String, Object>>> removeFromCart(@PathVariable Long productId) {
        return cartService.removeProduct(productId)
                .thenReturn(createSuccessResponse());
    }

    private ResponseEntity<Map<String, Object>> createSuccessResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        return ResponseEntity.ok(response);
    }

}
