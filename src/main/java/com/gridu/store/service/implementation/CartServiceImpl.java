package com.gridu.store.service.implementation;

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
import com.gridu.store.repository.CartRepo;
import com.gridu.store.repository.ProductRepo;
import com.gridu.store.service.CartService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProductRepo productRepo;
    private final CartRepo cartRepo;
    private final AuthServiceImpl authServiceImpl;
    private final ProductServiceImpl productService;

    @Transactional
    @Override
    public ProductResponseDto addItemToCart(UserCartRequestDto requestDto, String authHeader) {
        UserEntity userEntity = authServiceImpl.getUserEntityByToken(authHeader);
        ProductEntity productEntity = productService.getProductEntity(requestDto.getId());
        addProductToCart(requestDto, userEntity, productEntity);
        return getProductResponseDto(productEntity, requestDto.getQuantity());
    }

    @Override
    public CartResponseDto getCart(String authHeader) {
        List<ProductForCartResponse> products = new ArrayList<>();
        Long productsNumber = 0L;
        double totalPrice = 0;
        UserEntity userEntity = authServiceImpl.getUserEntityByToken(authHeader);
        List<CartEntity> allCartByUser = cartRepo.findAllByUserAndCartStatus(userEntity, CartStatus.ADDED_TO_CART);
        for (CartEntity cart : allCartByUser) {
            productsNumber++;
            ProductEntity product = cart.getProduct();
            double subtotalPrice = product.getPrice() * cart.getQuantity();
            products.add(getProductForCartResponse(productsNumber, cart, product, subtotalPrice));
            totalPrice += subtotalPrice;
        }
        return getCartResponseDto(products, totalPrice);
    }

    @Transactional
    @Override
    public Boolean deleteProductFromCart(Long id, String authHeader) {
        UserEntity userEntity = authServiceImpl.getUserEntityByToken(authHeader);
        ProductEntity productEntity = productService.getProductEntity(id);
        CartEntity cartEntity = getCartByUserAndProductIdWhithStatusAddedToCart(userEntity, id);

        cartRepo.delete(cartEntity);
        productEntity.setAvailable(productEntity.getAvailable() + cartEntity.getQuantity());
        productRepo.save(productEntity);
        return true;
    }

    @Transactional
    @Override
    public ProductResponseDto modifyNumberOfItem(String authHeader, UserCartModifyDto requestDto) {
        UserEntity userEntity = authServiceImpl.getUserEntityByToken(authHeader);
        Long productId = requestDto.getProductId();
        ProductEntity productEntity = productService.getProductEntity(productId);
        CartEntity cartEntity = getCartByUserAndProductIdWhithStatusAddedToCart(userEntity, productId);

        checkingWhetherTheProductsQuantityIsAvailable(requestDto.getQuantity(), productEntity.getAvailable());

        cartEntity.setQuantity(requestDto.getQuantity());
        cartRepo.save(cartEntity);
        return getProductResponseDto(cartEntity.getProduct(), requestDto.getQuantity());
    }

    private static ProductResponseDto getProductResponseDto(ProductEntity productEntity, Long requestQuantity) {
        return ProductResponseDto.builder()
                .id(productEntity.getId())
                .title(productEntity.getTitle())
                .price(productEntity.getPrice())
                .available(requestQuantity)
                .build();
    }

    private static ProductForCartResponse getProductForCartResponse(
            Long productsNumber, CartEntity cart, ProductEntity product, double subtotalPrice) {
        return ProductForCartResponse.builder()
                .numberOfProduct(productsNumber)
                .title(product.getTitle())
                .price(product.getPrice())
                .quantities(cart.getQuantity())
                .subtotalPrice(subtotalPrice)
                .build();
    }

    private static CartResponseDto getCartResponseDto(List<ProductForCartResponse> products, double totalPrice) {
        return CartResponseDto.builder()
                .products(products)
                .totalPrice(totalPrice)
                .build();
    }

    private void checkingWhetherTheProductsQuantityIsAvailable(Long needQuantity, Long available) {
        if (available < needQuantity) {
            throw new ApiException(Exceptions.PRODUCTS_NOT_ENOUGH);
        }
    }

    private void addProductToCart(UserCartRequestDto requestDto, UserEntity userEntity, ProductEntity productEntity) {
        boolean cartEntityExist = false;
        CartEntity existCart = cartRepo.findByUserAndProductIdAndCartStatus(
                userEntity, requestDto.getId(), CartStatus.ADDED_TO_CART).orElse(null);
        if (existCart != null) {
            long needQuantity = existCart.getQuantity() + requestDto.getQuantity();
            checkingWhetherTheProductsQuantityIsAvailable(needQuantity, productEntity.getAvailable());
            existCart.setQuantity(needQuantity);
            cartRepo.save(existCart);
            cartEntityExist = true;
        }
        if (!cartEntityExist) {
            checkingWhetherTheProductsQuantityIsAvailable(requestDto.getQuantity(), productEntity.getAvailable());
            CartEntity cartEntity = CartEntity.builder()
                    .user(userEntity)
                    .product(productEntity)
                    .quantity(requestDto.getQuantity())
                    .cartStatus(CartStatus.ADDED_TO_CART)
                    .build();
            cartRepo.save(cartEntity);
        }
    }

    private CartEntity getCartByUserAndProductIdWhithStatusAddedToCart(UserEntity userEntity, Long productId) {
        return cartRepo.findByUserAndProductIdAndCartStatus(userEntity, productId, CartStatus.ADDED_TO_CART)
                .orElseThrow(() -> new ApiException(Exceptions.PRODUCT_NOT_FOUND));
    }
}
