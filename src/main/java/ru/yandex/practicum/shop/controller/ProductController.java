package ru.yandex.practicum.shop.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.ProductService;
import ru.yandex.practicum.shop.service.impl.ProductServiceImpl;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/shop/product")
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ProductService productService;
    private final OrderService orderService;

    public ProductController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
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

        orderService.findLastActiveOrder().ifPresent(order -> {
            Map<Long, Integer> productIdQuantityMap = order.getItems().stream().collect(
                    Collectors.toMap(
                            item -> item.getProduct().getId(),
                            OrderItem::getQuantity,
                            Integer::sum
                    )
            );
            enrichProductDtoList(products, productIdQuantityMap);
        });

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

    @GetMapping("/{productId}")
    public String productCard(Model model, @PathVariable Long productId) {
        var product = productService.getProductById(productId).orElseThrow(
                () -> new IllegalArgumentException("Продукт с id = " + productId + " не найден.")
        );
        orderService.findLastActiveOrder()
                .flatMap(order -> order.getItems().stream()
                        .filter(oi -> productId.equals(oi.getProduct().getId()))
                        .findFirst()).ifPresent(oi -> {
                    product.setQuantity(oi.getQuantity());
                    product.setInCart(true);
                });
        model.addAttribute("product", product);
        return "product";
    }

    @GetMapping("/add")
    public String addNewProductPage(Model model) {
        model.addAttribute("product", new Product());
        return "new-product";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("product") ProductDto productDto,
                             @RequestParam("imageFile") MultipartFile file) {
        productService.saveProductWithImage(productDto, file);
        return "redirect:/shop/product";
    }

    private void enrichProductDtoList(Page<ProductDto> products, Map<Long, Integer> productMap) {
        products.forEach(productDto -> {
            if (productMap.containsKey(productDto.getId())) {
                productDto.setInCart(true);
                productDto.setQuantity(productMap.get(productDto.getId()));
            }
        });
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
