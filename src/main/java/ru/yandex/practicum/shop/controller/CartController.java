package ru.yandex.practicum.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.service.CartService;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.util.OrderUtil;

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
    public String cart(Model model) {
        var cartOptional = cartService.getCart();
        model.addAttribute("empty", cartOptional.isEmpty());
        var cart = cartOptional.orElse(new Order());

        if (cartOptional.isPresent()) {
            var total = OrderUtil.getTotal(cart);
            model.addAttribute("total", String.format("%.2f", total));
        }
        model.addAttribute("cart", cart);

        return "cart";
    }

    @GetMapping("/checkout/{orderId}")
    public String checkoutPage(Model model, @PathVariable Long orderId) {
        var order = orderService.findByIdAndStatus(orderId, OrderStatus.CHECKOUT).orElseThrow(
                () -> new IllegalArgumentException("Заказ с id = " + orderId + " не найден")
        );
        var total = OrderUtil.getTotal(order);
        model.addAttribute("order", order);
        model.addAttribute("total", String.format("%.2f", total));
        return "checkout";
    }

    @PostMapping("/checkout/{orderId}")
    public String moveOrderToCheckoutStage(@PathVariable Long orderId) {
        cartService.moveCartToCheckout(orderId);
        return "redirect:/cart/checkout/" + orderId;
    }

    @PostMapping("/purchase/{orderId}")
    public String confirmPurchase(@PathVariable Long orderId) {
        cartService.confirmPurchase(orderId);
        return "redirect:/order/summary/" + orderId;
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<Map<String, Object>> addToCart(@PathVariable Long productId) {
        cartService.addProduct(productId);
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update/{productId}")
    public ResponseEntity<Map<String, Object>> updateQuantity(@PathVariable Long productId, @RequestParam("change") int quantity) {
        cartService.updateQuantity(productId, quantity);
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove/{productId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long productId) {
        cartService.removeProduct(productId);
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        return ResponseEntity.ok(response);
    }

}
