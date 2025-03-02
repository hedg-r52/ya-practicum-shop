package ru.yandex.practicum.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.Image;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderItem;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.mapper.ProductMapper;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.ProductService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    OrderService orderService;

    @Test
    void whenGetProductList_shouldGetProductList() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);
        var products = getProducts();
        Page<ProductDto> productPage = new PageImpl<>(products, pageable, products.size());

        when(productService.findAll(any(Pageable.class)))
                .thenReturn(productPage);

        mockMvc.perform(get("/shop/product"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("product-showcase"));

        verify(productService, times(1)).findAll(any(Pageable.class));
        verify(orderService, times(1)).findLastActiveOrder();
    }

    @Test
    void whenGetProductListFiltered_shouldGetProductList() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);
        var products = getProducts();
        Page<ProductDto> productPage = new PageImpl<>(products, pageable, products.size());

        when(productService.findAllByNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(productPage);

        mockMvc.perform(get("/shop/product")
                        .param("search", "Product"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("product-showcase"));

        verify(productService, times(1)).findAllByNameContainingIgnoreCase(anyString(), any(Pageable.class));
        verify(orderService, times(1)).findLastActiveOrder();
    }

    @Test
    void whenGetProductCard_shouldGetProduct() throws Exception {
        var productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setName("Product 1");
        productDto.setDescription("Description of Product 1");

        when(productService.getProductById(anyLong()))
                .thenReturn(Optional.of(productDto));
        mockMvc.perform(get("/shop/product/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("product"));

        verify(productService, times(1)).getProductById(anyLong());
        verify(orderService, times(1)).findLastActiveOrder();
    }

    @Test
    void whenGetAddProduct_shouldReceiveNewProductView() throws Exception {
        mockMvc.perform(get("/shop/product/add"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("new-product"));
    }

    @Test
    void whenPost() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "image.jpg",
                "image/jpeg",
                new byte[0]
        );

        mockMvc.perform(multipart("/shop/product/add")
                        .file(imageFile))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/shop/product"));

        verify(productService, times(1))
                .saveProductWithImage(any(ProductDto.class), any(MultipartFile.class));

    }

    private List<ProductDto> getProducts() {
        var productDto1 = new ProductDto();
        productDto1.setId(1L);
        productDto1.setName("Product 1");
        productDto1.setDescription("Description of Product 1");

        var productDto2 = new ProductDto();
        productDto2.setId(1L);
        productDto2.setName("Product 1");
        productDto2.setDescription("Description of Product 1");

        return new ArrayList<>(List.of(productDto1, productDto2));
    }

}