package com.gridu.store.service;

import com.gridu.store.dto.request.UserCartModifyDto;
import com.gridu.store.dto.request.UserCartRequestDto;
import com.gridu.store.dto.response.CartResponseDto;
import com.gridu.store.dto.response.ProductResponseDto;

public interface CartService {

    ProductResponseDto addItemToCart(UserCartRequestDto requestDto, String authHeader);

    CartResponseDto getCart(String authHeader);

    Boolean deleteProductFromCart(Long id, String authHeader);

    ProductResponseDto modifyNumberOfItem(String authHeader, UserCartModifyDto requestDto);
}
