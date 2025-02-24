package ru.yandex.practicum.shop.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.service.ProductService;
import ru.yandex.practicum.shop.service.impl.ProductServiceImpl;

import java.util.Optional;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/shop/product")
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ProductService productService;

    public ProductController(ProductServiceImpl productService) {
        this.productService = productService;
    }

    @GetMapping
    public String productList(
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(DEFAULT_PAGE_SIZE);

        Page<ProductDto> products = productService.findAll(PageRequest.of(currentPage - 1, pageSize));
        model.addAttribute("products", products);

        int totalPages = products.getTotalPages();
        if (totalPages > 0) {
            long pageNumbers = IntStream.rangeClosed(1, totalPages).count();
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "product-showcase";
    }
}
