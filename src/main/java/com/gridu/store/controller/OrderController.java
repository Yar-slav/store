package com.gridu.store.controller;

import com.gridu.store.dto.response.MessageResponseDto;
import com.gridu.store.dto.response.OrderResponseDto;
import com.gridu.store.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PutMapping("/checkout")
    public ResponseEntity<MessageResponseDto> checkout(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(orderService.checkout(authHeader));
    }

    @PatchMapping("/cancel/{number-of-order}")
    public ResponseEntity<MessageResponseDto> cancelOrder(
            @PathVariable(name = "number-of-order") Long numberOfOrder,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(orderService.cancelOrder(numberOfOrder, authHeader));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllOrder(
            @RequestHeader("Authorization") String authHeader) {
    return ResponseEntity.ok(orderService.getAllOrder(authHeader));
    }
}
