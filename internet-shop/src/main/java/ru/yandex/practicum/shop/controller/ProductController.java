package ru.yandex.practicum.shop.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.OrderItemDto;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.ProductService;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/shop/product")
public class ProductController {

    private static final String PRODUCT_ATTR = "product";
    private static final String PRODUCT_VIEW = "product";

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ProductService productService;
    private final OrderService orderService;

    public ProductController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping
    public Mono<String> productList(
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

        Mono<Page<ProductDto>> products;
        if (StringUtils.hasText(searchString)) {
            products = productService.findAllByNameContainingIgnoreCase(searchString, PageRequest.of(currentPage - 1, pageSize, sort));
        } else {
            products = productService.findAll(PageRequest.of(currentPage - 1, pageSize, sort));
        }

        return orderService.findLastActiveOrder()
                .flatMap(order -> {
                    Map<Long, Integer> productIdQuantityMap = order.getOrderItems().stream()
                            .collect(Collectors.toMap(
                                    OrderItemDto::getProductId,
                                    OrderItemDto::getQuantity,
                                    Integer::sum
                            ));
                    return products.doOnNext(productList -> enrichProductDtoList(productList, productIdQuantityMap));
                })
                .switchIfEmpty(products)
                .doOnNext(productList -> {
                    model.addAttribute("products", productList);
                    model.addAttribute("view", view);
                    model.addAttribute("search", searchString);
                    model.addAttribute("sortBy", sortBy);
                    int totalPages = productList.getTotalPages();
                    if (totalPages > 0) {
                        long pageNumbers = IntStream.rangeClosed(1, totalPages).count();
                        model.addAttribute("pageNumbers", pageNumbers);
                    }
                })
                .thenReturn("product-showcase");
    }

    @GetMapping("/{productId}")
    public Mono<String> productCard(Model model, @PathVariable Long productId) {
        return productService.getProductById(productId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Продукт с id = " + productId + " не найден.")))
                .flatMap(product -> orderService.findLastActiveOrder()
                        .flatMap(order -> Flux.fromIterable(order.getOrderItems())
                                .filter(oi -> productId.equals(oi.getProductId()))
                                .next()
                                .doOnNext(oi -> {
                                    product.setQuantity(oi.getQuantity());
                                    product.setInCart(true);
                                }))
                        .thenReturn(product)
                )
                .switchIfEmpty(Mono.defer(() -> productService.getProductById(productId)))
                .doOnNext(product -> model.addAttribute(PRODUCT_ATTR, product))
                .thenReturn(PRODUCT_VIEW);
    }

    @GetMapping("/add")
    public Mono<String> addNewProductPage(Model model) {
        return Mono.fromRunnable(() -> model.addAttribute(PRODUCT_ATTR, new ProductDto()))
                .thenReturn("new-product");
    }

    @PostMapping("/add")
    public Mono<String> addProduct(@ModelAttribute("product") ProductDto productDto,
                             @RequestPart("imageFile") Mono<FilePart> fileMono) {
        return fileMono.flatMap(file -> productService.saveProductWithImage(productDto, file))
                .thenReturn("redirect:/shop/product");
    }

    private void enrichProductDtoList(Page<ProductDto> products, Map<Long, Integer> productMap) {
        products.forEach(productDto -> {
            if (productMap.containsKey(productDto.getId())) {
                productDto.setInCart(true);
                productDto.setQuantity(productMap.get(productDto.getId()));
            } else {
                productDto.setInCart(false);
                productDto.setQuantity(0);
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
