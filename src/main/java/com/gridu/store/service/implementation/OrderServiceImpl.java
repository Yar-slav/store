package com.gridu.store.service.implementation;

import com.gridu.store.dto.response.MessageResponseDto;
import com.gridu.store.dto.response.OrderResponseDto;
import com.gridu.store.exception.ApiException;
import com.gridu.store.exception.Exceptions;
import com.gridu.store.model.CartEntity;
import com.gridu.store.model.CartStatus;
import com.gridu.store.model.ProductEntity;
import com.gridu.store.model.UserEntity;
import com.gridu.store.repository.CartRepo;
import com.gridu.store.repository.ProductRepo;
import com.gridu.store.service.OrderService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final AuthServiceImpl authServiceImpl;
    private final CartRepo cartRepo;
    private final ProductRepo productRepo;

    @Transactional
    @Override
    public MessageResponseDto checkout(String authHeader) {
        UserEntity userEntity = authServiceImpl.getUserEntityByToken(authHeader);
        List<CartEntity> allCartByUser = cartRepo.findAllByUserAndCartStatus(userEntity, CartStatus.ADDED_TO_CART);
        Long orderId = generateOrderId();
        LocalDateTime orderedOn = LocalDateTime.now();
        for (CartEntity cart : allCartByUser) {
            ProductEntity product = cart.getProduct();
            if (product.getAvailable() < cart.getQuantity()) {
                throw new ApiException(Exceptions.PRODUCTS_NOT_ENOUGH);
            }
            product.setAvailable(product.getAvailable() - cart.getQuantity());
            productRepo.save(product);

            cart.setOrderedOn(orderedOn);
            cart.setOrderId(orderId);
            cart.setCartStatus(CartStatus.ORDER_PLACED);
            cartRepo.save(cart);
        }
        return new MessageResponseDto("The order has been placed successfully");
    }

    @Override
    public MessageResponseDto cancelOrder(Long orderId, String authHeader) {
        UserEntity userEntity = authServiceImpl.getUserEntityByToken(authHeader);
        List<CartEntity> cartsByOrderId = getCartsByOrderIdWithStatusOrderPlaced(orderId);
        LocalDateTime canceledOn = LocalDateTime.now();
        for (CartEntity cart : cartsByOrderId) {
            if (!cart.getUser().equals(userEntity)) {
                throw new ApiException(Exceptions.ORDER_NOT_BELONG_USER);
            }
            ProductEntity product = cart.getProduct();
            product.setAvailable(product.getAvailable() + cart.getQuantity());
            productRepo.save(product);

            cart.setCanceledOn(canceledOn);
            cart.setCartStatus(CartStatus.CANCEL);
            cartRepo.save(cart);
        }
        return new MessageResponseDto("The order: " + orderId + " has been canceled successfully");
    }

    @Override
    public List<OrderResponseDto> getAllOrder(String authHeader) {
        List<OrderResponseDto> orderResponseDtoList = new ArrayList<>();
        UserEntity userEntity = authServiceImpl.getUserEntityByToken(authHeader);
        List<CartEntity> carts = cartRepo.findAllByUser(userEntity);
        Map<Long, List<CartEntity>> map = carts.stream()
                .filter(cart -> !cart.getCartStatus().equals(CartStatus.ADDED_TO_CART))
                .collect(Collectors.groupingBy(CartEntity::getOrderId));

        for (Map.Entry<Long, List<CartEntity>> cartEntities : map.entrySet()) {
            orderResponseDtoList.add(getOrderResponseDto(cartEntities));
        }
        orderResponseDtoList = orderResponseListSortedByDate(orderResponseDtoList);
        return orderResponseDtoList;
    }

    private static OrderResponseDto getOrderResponseDto(Map.Entry<Long, List<CartEntity>> cartEntities) {
        CartEntity cart = cartEntities.getValue().get(0);
        return OrderResponseDto.builder()
                .orderId(cartEntities.getKey())
                .status(cart.getCartStatus())
                .date(getDateTime(cart))
                .totalPrice(getOrderTotalPrice(cartEntities.getValue()))
                .build();
    }

    private static LocalDateTime getDateTime(CartEntity cart) {
        if (cart.getCartStatus().equals(CartStatus.CANCEL)) {
            return cart.getCanceledOn();
        } else {
            return cart.getOrderedOn();
        }
    }

    private static double getOrderTotalPrice(List<CartEntity> carts) {
        double totalPrice = 0.0;
        for (CartEntity cart : carts) {
            totalPrice += cart.getProduct().getPrice() * cart.getQuantity();
        }
        return totalPrice;
    }

    private List<CartEntity> getCartsByOrderIdWithStatusOrderPlaced(Long orderId) {
        List<CartEntity> cartsByOrderId = cartRepo.findAllByOrderIdAndCartStatus(orderId, CartStatus.ORDER_PLACED);
        if (cartsByOrderId.equals(Collections.emptyList())) {
            throw new ApiException(Exceptions.ORDER_NOT_FOUND);
        }
        return cartsByOrderId;
    }

    private Long generateOrderId() {
        Long orderId;
        do {
            orderId = Math.abs(new Random().nextLong());
        } while (!cartRepo.findAllByOrderId(orderId).equals(Collections.emptyList()));
        return orderId;
    }

    private static List<OrderResponseDto> orderResponseListSortedByDate(List<OrderResponseDto> orderResponseDtoList) {
        return orderResponseDtoList.stream()
                .sorted(Comparator.comparing(OrderResponseDto::getDate).reversed())
                .collect(Collectors.toList());
    }
}
