package ru.yandex.practicum.shop.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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
            @RequestParam("size") Optional<Integer> size,
            @RequestParam(value = "sortBy", defaultValue = "alphabet_asc") String sortBy,
            @RequestParam(value = "view", defaultValue = "grid") String view,
            @RequestParam("search") Optional<String> search) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(DEFAULT_PAGE_SIZE);
        String searchString = search.orElse("");

        Sort sort = getSortByString(sortBy);

        Page<ProductDto> products;
        if (StringUtils.hasText(searchString)) {
            products = productService.findAllByNameContainingIgnoreCase(searchString, PageRequest.of(currentPage - 1, pageSize, sort));
        } else {
            products = productService.findAll(PageRequest.of(currentPage - 1, pageSize, sort));
        }
        model.addAttribute("products", products);
        model.addAttribute("view", view);
        model.addAttribute("search", searchString);
        model.addAttribute("sortBy", sortBy);

        int totalPages = products.getTotalPages();
        if (totalPages > 0) {
            long pageNumbers = IntStream.rangeClosed(1, totalPages).count();
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "product-showcase";
    }

    private Sort getSortByString(String sortString) {
        return switch (sortString) {
            case "alphabet_asc" -> Sort.by("name").ascending();
            case "alphabet_desc" -> Sort.by("name").descending();
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            default -> Sort.unsorted();
        };
    }
}
