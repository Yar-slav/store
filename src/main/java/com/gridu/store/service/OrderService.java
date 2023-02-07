package com.gridu.store.service;

import com.gridu.store.dto.response.MessageResponseDto;
import com.gridu.store.dto.response.OrderResponseDto;
import java.util.List;

public interface OrderService {

    MessageResponseDto checkout(String authHeader);

    MessageResponseDto cancelOrder(Long numberOfOrder, String authHeader);

    List<OrderResponseDto> getAllOrder(String authHeader);
}
