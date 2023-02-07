package com.gridu.store.service.implementation;

import static com.gridu.store.factory.dto.ProductResponseDtoFactory.createProductResponseDTOs;
import static com.gridu.store.factory.model.ProductEntityFactory.createProductsEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.gridu.store.dto.request.ProductRequestDto;
import com.gridu.store.dto.response.ProductResponseDto;
import com.gridu.store.exception.ApiException;
import com.gridu.store.exception.Exceptions;
import com.gridu.store.mapper.ProductMapper;
import com.gridu.store.model.ProductEntity;
import com.gridu.store.repository.ProductRepo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private final String token = "Bearer token";

    @Mock
    private ProductRepo productRepo;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getAllProducts() {
        List<ProductResponseDto> productResponseDTOs = createProductResponseDTOs();
        List<ProductEntity> productsEntity = createProductsEntity();
        Pageable pageable = PageRequest.of(0, 3);
        Page<ProductEntity> pageEntity = new PageImpl<>(productsEntity);

        when(productRepo.findAll(pageable)).thenReturn(pageEntity);
        for (int i = 0; i < productsEntity.size(); i++) {
            when(productMapper.toProductResponseDto(productsEntity.get(i)))
                    .thenReturn(productResponseDTOs.get(i));
        }

        List<ProductResponseDto> result = productService.getAll(pageable);
        assertEquals(result.size(), 3);
    }

    @Test
    void addProduct_ifProductNotExist() {
        ProductResponseDto responseDto = new ProductResponseDto(1L, "book", 10L, 300);
        ProductRequestDto requestDto = new ProductRequestDto("book", 10L, 300);
        ProductEntity productEntity = new ProductEntity(null, "book", 10L, 300, null);
        ProductEntity productEntityAfterSave = new ProductEntity(1L, "book", 10L, 300, null);

        when(productMapper.toProductEntity(requestDto)).thenReturn(productEntity);
        when(productRepo.findByTitleAndPrice("book", 300)).thenReturn(null);
        when(productRepo.save(productEntity)).thenReturn(productEntityAfterSave);
        when(productMapper.toProductResponseDto(productEntityAfterSave)).thenReturn(responseDto);

        ProductResponseDto result = productService.addProduct(requestDto, token);
        assertEquals(responseDto, result);
    }

    @Test
    void addProduct_ifProductExist() {
        ProductResponseDto responseDto = new ProductResponseDto(2L, "book", 11L, 300);
        ProductRequestDto requestDto = new ProductRequestDto("book", 1L, 300);
        ProductEntity productEntity = new ProductEntity(null, "book", 1L, 300, null);
        ProductEntity byTitleAndPrice = new ProductEntity(2L, "book", 10L, 300, null);

        when(productMapper.toProductEntity(requestDto)).thenReturn(productEntity);
        when(productRepo.findByTitleAndPrice("book", 300)).thenReturn(byTitleAndPrice);
        byTitleAndPrice.setAvailable(11L);
        when(productRepo.save(byTitleAndPrice)).thenReturn(byTitleAndPrice);
        when(productMapper.toProductResponseDto(byTitleAndPrice)).thenReturn(responseDto);

        ProductResponseDto result = productService.addProduct(requestDto, token);
        assertEquals(responseDto, result);
    }

    @Test
    void getProductEntity_IfProductExist() {
        ProductEntity productEntity = new ProductEntity(1L, "book", 1L, 300, null);
        when(productRepo.findById(productEntity.getId())).thenReturn(Optional.of(productEntity));

        ProductEntity result = productService.getProductEntity(productEntity.getId());
        assertEquals(productEntity, result);
    }

    @Test
    void getProductEntity_IfProductNotExist() {
        when(productRepo.findById(1L)).thenThrow(new ApiException(Exceptions.PRODUCT_NOT_FOUND));

        ApiException apiException = assertThrows(ApiException.class,
                () -> productService.getProductEntity(1L));

        assertEquals(Exceptions.PRODUCT_NOT_FOUND, apiException.getExceptions());
    }
}