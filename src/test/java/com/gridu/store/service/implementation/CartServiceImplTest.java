package com.gridu.store.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.gridu.store.dto.request.UserCartModifyDto;
import com.gridu.store.dto.request.UserCartRequestDto;
import com.gridu.store.dto.response.CartResponseDto;
import com.gridu.store.dto.response.ProductForCartResponse;
import com.gridu.store.dto.response.ProductResponseDto;
import com.gridu.store.exception.ApiException;
import com.gridu.store.exception.Exceptions;
import com.gridu.store.model.CartEntity;
import com.gridu.store.model.CartStatus;
import com.gridu.store.model.ProductEntity;
import com.gridu.store.model.UserEntity;
import com.gridu.store.model.UserRole;
import com.gridu.store.repository.CartRepo;
import com.gridu.store.repository.ProductRepo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    private final String token = "Bearer token";

    @Mock
    private ProductRepo productRepo;
    @Mock
    private CartRepo cartRepo;
    @Mock
    private AuthServiceImpl authServiceImpl;
    @Mock
    private ProductServiceImpl productService;
    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void AddProductToCart() {
        UserCartRequestDto userCartRequestDto = new UserCartRequestDto(5L, 10L);
        ProductResponseDto productResponseDto = new ProductResponseDto(5L, "book", 10L, 300);
        ProductEntity productEntity = new ProductEntity(5L, "book", 100L, 300, null);
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        CartEntity cartEntity = new CartEntity(null, user, productEntity, 10L, CartStatus.ADDED_TO_CART, null, null, null);
        CartEntity cartEntityAfterSave = new CartEntity(1L, user, productEntity, 10L, CartStatus.ADDED_TO_CART, null, null, null);

        when(authServiceImpl.getUserEntityByToken(token)).thenReturn(user);
        when(productService.getProductEntity(userCartRequestDto.getId())).thenReturn(productEntity);
        when(cartRepo.findByUserAndProductIdAndCartStatus(user, userCartRequestDto.getId(), CartStatus.ADDED_TO_CART))
                .thenReturn(Optional.empty());
        when(cartRepo.save(cartEntity)).thenReturn(cartEntityAfterSave);

        ProductResponseDto result = cartService.addItemToCart(userCartRequestDto, token);
        assertEquals(productResponseDto, result);
    }

    @Test
    void addProductToCart_ifProductAlreadyAddedToCart() {
        UserCartRequestDto userCartRequestDto = new UserCartRequestDto(5L, 10L);
        ProductResponseDto productResponseDto = new ProductResponseDto(5L, "book", 10L, 300);
        ProductEntity productEntity = new ProductEntity(5L, "book", 100L, 300, null);
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        CartEntity existCart = new CartEntity(1L, user, productEntity, 5L, CartStatus.ADDED_TO_CART, null, null, null);
        CartEntity cartEntity = new CartEntity(1L, user, productEntity, 15L, CartStatus.ADDED_TO_CART, null, null, null);

        when(authServiceImpl.getUserEntityByToken(token)).thenReturn(user);
        when(productService.getProductEntity(userCartRequestDto.getId())).thenReturn(productEntity);
        when(cartRepo.findByUserAndProductIdAndCartStatus(user, userCartRequestDto.getId(), CartStatus.ADDED_TO_CART)).thenReturn(Optional.of(existCart));
        when(cartRepo.save(cartEntity)).thenReturn(cartEntity);

        ProductResponseDto result = cartService.addItemToCart(userCartRequestDto, token);
        assertEquals(productResponseDto, result);
    }

    @Test
    void addItemToCart_IfProductsQuantityNotEnough() {
        UserCartRequestDto userCartRequestDto = new UserCartRequestDto(5L, 100L);
        ProductEntity productEntity = new ProductEntity(5L, "book", 10L, 300, null);
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);

        when(authServiceImpl.getUserEntityByToken(token)).thenReturn(user);
        when(productService.getProductEntity(userCartRequestDto.getId())).thenReturn(productEntity);
        when(cartRepo.findByUserAndProductIdAndCartStatus(user, userCartRequestDto.getId(), CartStatus.ADDED_TO_CART)).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class,
                () -> cartService.addItemToCart(userCartRequestDto, token));

        assertEquals(Exceptions.PRODUCTS_NOT_ENOUGH, apiException.getExceptions());
    }

    @Test
    void addItemToCart_IfProductNotFound() {
        UserCartRequestDto userCartRequestDto = new UserCartRequestDto(5L, 100L);
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);

        when(authServiceImpl.getUserEntityByToken(token)).thenReturn(user);
        when(productService.getProductEntity(userCartRequestDto.getId()))
                .thenThrow(new ApiException(Exceptions.PRODUCT_NOT_FOUND));

        ApiException apiException = assertThrows(ApiException.class,
                () -> cartService.addItemToCart(userCartRequestDto, token));

        assertEquals(Exceptions.PRODUCT_NOT_FOUND, apiException.getExceptions());
    }

    @Test
    void getCart() {
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);

        List<ProductForCartResponse> products = List.of(
                new ProductForCartResponse(1L, "book1", 300, 10L, 3000),
                new ProductForCartResponse(2L, "book2", 500, 10L, 5000)
        );
        ProductEntity productEntity1 = new ProductEntity(5L, "book1", 10L, 300, null);
        ProductEntity productEntity2 = new ProductEntity(6L, "book2", 10L, 500, null);
        List<CartEntity> allCartByUser = List.of(
                new CartEntity(1L, null, productEntity1, 10L, CartStatus.ADDED_TO_CART, null, null, null),
                new CartEntity(2L, null, productEntity2, 10L, CartStatus.ADDED_TO_CART, null, null, null)
        );
        CartResponseDto cartResponseDto = new CartResponseDto(products, 8000);

        when(authServiceImpl.getUserEntityByToken(token)).thenReturn(user);
        when(cartRepo.findAllByUserAndCartStatus(user, CartStatus.ADDED_TO_CART)).thenReturn(allCartByUser);

        CartResponseDto result = cartService.getCart(token);
        assertEquals(cartResponseDto, result);
    }

    @Test
    void deleteProductFromCart() {
        Long productId = 5L;
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        ProductEntity productEntity = new ProductEntity(productId, "book", 10L, 300, null);
        ProductEntity productEntityAfterSave = new ProductEntity(productId, "book", 20L, 300, null);
        CartEntity cartEntity = new CartEntity(1L, user, productEntity, 10L, CartStatus.ADDED_TO_CART, null, null, null);

        when(authServiceImpl.getUserEntityByToken(token)).thenReturn(user);
        when(productService.getProductEntity(productId)).thenReturn(productEntity);
        when(cartRepo.findByUserAndProductIdAndCartStatus(user, productId, CartStatus.ADDED_TO_CART)).thenReturn(Optional.of(cartEntity));
        when(productRepo.save(productEntityAfterSave)).thenReturn(productEntityAfterSave);

        assertTrue(cartService.deleteProductFromCart(productId, token));
    }

    @Test
    void modifyNumberOfItem() {
        UserCartModifyDto requestDto = new UserCartModifyDto(5L, 100L);
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        ProductEntity productEntity = new ProductEntity(5L, "book", 200L, 300, null);
        CartEntity cartEntity = new CartEntity(1L, user, productEntity, 10L, CartStatus.ADDED_TO_CART, null, null, null);
        CartEntity cartEntityAfterSave = new CartEntity(1L, user, productEntity, 100L, CartStatus.ADDED_TO_CART, null, null, null);
        ProductResponseDto responseDto = new ProductResponseDto(5L, "book", 100L, 300);

        when(authServiceImpl.getUserEntityByToken(token)).thenReturn(user);
        when(productService.getProductEntity(requestDto.getProductId())).thenReturn(productEntity);
        when(cartRepo.findByUserAndProductIdAndCartStatus(user, requestDto.getProductId(), CartStatus.ADDED_TO_CART)).thenReturn(Optional.of(cartEntity));
        when(cartRepo.save(cartEntityAfterSave)).thenReturn(cartEntityAfterSave);

        ProductResponseDto result = cartService.modifyNumberOfItem(token, requestDto);
        assertEquals(responseDto, result);
    }

    @Test
    void modifyNumberOfItem_ifProductNotFound() {
        UserCartModifyDto requestDto = new UserCartModifyDto(5L, 100L);
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        ProductEntity productEntity = new ProductEntity(5L, "book", 200L, 300, null);

        when(authServiceImpl.getUserEntityByToken(token)).thenReturn(user);
        when(productService.getProductEntity(requestDto.getProductId())).thenReturn(productEntity);
        lenient().when(cartRepo.findByUserAndProductIdAndCartStatus(user, 4L, CartStatus.ADDED_TO_CART)).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class,
                () -> cartService.modifyNumberOfItem(token, requestDto));

        assertEquals(Exceptions.PRODUCT_NOT_FOUND, apiException.getExceptions());
    }
}
