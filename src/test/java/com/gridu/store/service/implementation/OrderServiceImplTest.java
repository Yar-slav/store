package com.gridu.store.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.gridu.store.dto.response.MessageResponseDto;
import com.gridu.store.dto.response.OrderResponseDto;
import com.gridu.store.exception.ApiException;
import com.gridu.store.exception.Exceptions;
import com.gridu.store.model.CartEntity;
import com.gridu.store.model.CartStatus;
import com.gridu.store.model.ProductEntity;
import com.gridu.store.model.UserEntity;
import com.gridu.store.model.UserRole;
import com.gridu.store.repository.CartRepo;
import com.gridu.store.repository.ProductRepo;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private final String authHeader = "Bearer token";

    @Mock
    private AuthServiceImpl authServiceImpl;
    @Mock
    private CartRepo cartRepo;
    @Mock
    private ProductRepo productRepo;
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void checkout() {
        Long orderId = 10L;
        MessageResponseDto responseDto = new MessageResponseDto("The order has been placed successfully");
        LocalDateTime orderedOn = LocalDateTime.now();
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        ProductEntity productEntity1 = new ProductEntity(5L, "book1", 100L, 300, null);
        ProductEntity productEntity2 = new ProductEntity(6L, "book2", 100L, 500, null);
        List<CartEntity> allCartByUser = List.of(
                new CartEntity(1L, user, productEntity1, 10L, CartStatus.ADDED_TO_CART, null, null, null),
                new CartEntity(2L, user, productEntity2, 10L, CartStatus.ADDED_TO_CART, null, null, null)
        );
        List<CartEntity> allCartByUserAfterSave = List.of(
                new CartEntity(1L, user, productEntity1, 10L, CartStatus.ORDER_PLACED, orderId, orderedOn, null ),
                new CartEntity(2L, user, productEntity2, 10L, CartStatus.ORDER_PLACED, orderId, orderedOn, null)
        );
        when(authServiceImpl.getUserEntityByToken(authHeader)).thenReturn(user);
        when(cartRepo.findAllByUserAndCartStatus(user, CartStatus.ADDED_TO_CART)).thenReturn(allCartByUser);
        for (int i = 0; i < allCartByUser.size(); i++) {
            ProductEntity product = allCartByUser.get(i).getProduct();
            product.setAvailable(product.getAvailable() - allCartByUser.get(i).getQuantity());
            lenient().when(productRepo.save(product)).thenReturn(product);
            lenient().when(cartRepo.save(allCartByUser.get(i))).thenReturn(allCartByUserAfterSave.get(i));
        }
        MessageResponseDto result = orderService.checkout(authHeader);
        assertEquals(responseDto, result);
    }

    @Test
    void checkout_ifProductNotEnough() {
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        ProductEntity productEntity1 = new ProductEntity(5L, "book1", 100L, 300, null);
        ProductEntity productEntity2 = new ProductEntity(6L, "book2", 100L, 500, null);
        List<CartEntity> allCartByUser = List.of(
                new CartEntity(1L, user, productEntity1, 1000L, CartStatus.ADDED_TO_CART, null, null, null),
                new CartEntity(2L, user, productEntity2, 1000L, CartStatus.ADDED_TO_CART, null, null, null)
        );

        when(authServiceImpl.getUserEntityByToken(authHeader)).thenReturn(user);
        when(cartRepo.findAllByUserAndCartStatus(user, CartStatus.ADDED_TO_CART)).thenReturn(allCartByUser);
        ApiException apiException = assertThrows(ApiException.class,
                () -> orderService.checkout(authHeader));

        assertEquals(Exceptions.PRODUCTS_NOT_ENOUGH, apiException.getExceptions());
    }

    @Test
    void cancelOrder() {
        Long orderId = 10L;
        MessageResponseDto messageResponseDto = new MessageResponseDto("The order: " + orderId + " has been canceled successfully");
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        LocalDateTime orderedOn = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
        LocalDateTime canceledOn = LocalDateTime.of(2022, Month.JULY, 29, 20, 30, 40);

        ProductEntity productEntity1 = new ProductEntity(5L, "book1", 100L, 300, null);
        ProductEntity productEntity2 = new ProductEntity(6L, "book2", 100L, 500, null);
        List<CartEntity> cartsByOrderId = List.of(
                new CartEntity(1L, user, productEntity1, 10L, CartStatus.ORDER_PLACED, orderId, orderedOn, null),
                new CartEntity(2L, user, productEntity2, 10L, CartStatus.ORDER_PLACED, orderId, orderedOn, null)
        );
        ProductEntity productEntityReturned1 = new ProductEntity(5L, "book1", 110L, 300, null);
        ProductEntity productEntityReturned2 = new ProductEntity(6L, "book2", 110L, 500, null);
        List<CartEntity> cartsAfterCancel = List.of(
                new CartEntity(1L, user, productEntityReturned1, 10L, CartStatus.CANCEL, orderId, orderedOn, canceledOn),
                new CartEntity(2L, user, productEntityReturned2, 10L, CartStatus.CANCEL, orderId, orderedOn, canceledOn)
        );

        when(authServiceImpl.getUserEntityByToken(authHeader)).thenReturn(user);
        when(cartRepo.findAllByOrderIdAndCartStatus(orderId, CartStatus.ORDER_PLACED)).thenReturn(cartsByOrderId);
        for (CartEntity cart : cartsAfterCancel) {
            ProductEntity product = cart.getProduct();
            lenient().when(productRepo.save(product)).thenReturn(product);
            lenient().when(cartRepo.save(cart)).thenReturn(cart);
        }

        MessageResponseDto result = orderService.cancelOrder(orderId, authHeader);
        assertEquals(messageResponseDto, result);
    }
    @Test
    void cancelOrder_cartNotFound() {
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);

        when(authServiceImpl.getUserEntityByToken(authHeader)).thenReturn(user);
        ApiException apiException = assertThrows(ApiException.class,
                () -> orderService.cancelOrder(9L, authHeader));

        assertEquals(Exceptions.ORDER_NOT_FOUND, apiException.getExceptions());
    }

    @Test
    void cancelOrder_cartNotFound_cartNotBelongUser() {
        Long orderId = 10L;
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        UserEntity user2 = new UserEntity(2L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        LocalDateTime orderedOn = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
        ProductEntity productEntity1 = new ProductEntity(5L, "book1", 100L, 300, null);
        ProductEntity productEntity2 = new ProductEntity(6L, "book2", 100L, 500, null);
        List<CartEntity> cartsByOrderId = List.of(
                new CartEntity(1L, user, productEntity1, 10L, CartStatus.ORDER_PLACED, orderId, orderedOn, null),
                new CartEntity(2L, user, productEntity2, 10L, CartStatus.ORDER_PLACED, orderId, orderedOn, null)
        );

        when(authServiceImpl.getUserEntityByToken(authHeader)).thenReturn(user2);
        lenient().when(cartRepo.findAllByOrderIdAndCartStatus(orderId, CartStatus.ORDER_PLACED)).thenReturn(cartsByOrderId);
        ApiException apiException = assertThrows(ApiException.class,
                () -> orderService.cancelOrder(10L, authHeader));

        assertEquals(Exceptions.ORDER_NOT_BELONG_USER, apiException.getExceptions());
    }

    @Test
    void getAllOrder() {
        LocalDateTime canceledOn1 = LocalDateTime.of(2022, Month.JULY, 29, 18, 30, 40);
        LocalDateTime orderedOn2 = LocalDateTime.of(2022, Month.JULY, 29, 19, 30, 40);
        List<OrderResponseDto> responseDtos = List.of(
                new OrderResponseDto(9L, orderedOn2, 5000D, CartStatus.ORDER_PLACED),
                new OrderResponseDto(10L, canceledOn1, 4000D, CartStatus.CANCEL)
        );
        UserEntity user = new UserEntity(1L, "user@gmail.com", "passwordEncode", UserRole.USER, null);
        ProductEntity productEntity1 = new ProductEntity(5L, "book1", 10L, 300, null);
        ProductEntity productEntity2 = new ProductEntity(6L, "book2", 10L, 400, null);
        ProductEntity productEntity3 = new ProductEntity(7L, "book3", 10L, 500, null);
        List<CartEntity> cartsByOrderId = List.of(
                new CartEntity(1L, user, productEntity1, 10L, CartStatus.ADDED_TO_CART, null, null, null),
                new CartEntity(2L, user, productEntity2, 10L, CartStatus.CANCEL, 10L, any(), canceledOn1),
                new CartEntity(3L, user, productEntity3, 10L, CartStatus.ORDER_PLACED, 9L, orderedOn2, null)
        );

        when(authServiceImpl.getUserEntityByToken(authHeader)).thenReturn(user);
        when(cartRepo.findAllByUser(user)).thenReturn(cartsByOrderId);

        List<OrderResponseDto> result = orderService.getAllOrder(authHeader);
        assertEquals(responseDtos, result);
    }
}