package com.gridu.store.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.gridu.store.dto.request.UserLoginRequest;
import com.gridu.store.dto.request.UserRegistrationRequestDto;
import com.gridu.store.dto.response.LoginResponseDto;
import com.gridu.store.dto.response.MessageResponseDto;
import com.gridu.store.exception.ApiException;
import com.gridu.store.exception.Exceptions;
import com.gridu.store.model.CartStatus;
import com.gridu.store.model.UserEntity;
import com.gridu.store.model.UserRole;
import com.gridu.store.repository.CartRepo;
import com.gridu.store.repository.UserRepo;
import com.gridu.store.secure.config.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private CartRepo cartRepo;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerIfUserNotExist() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto("user@gmail.com", "password");
        MessageResponseDto responseDto = new MessageResponseDto("User with email: user@gmail.com is successfully registered");
        String passwordEncode = "passwordEncode";
        UserEntity userEntity = new UserEntity(null, requestDto.getEmail(), passwordEncode, UserRole.USER, null);
        UserEntity userSave = new UserEntity(1L, requestDto.getEmail(), passwordEncode, UserRole.USER, null);

        when(userRepo.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn(passwordEncode);
        when(userRepo.save(userEntity)).thenReturn(userSave);

        MessageResponseDto result = authService.register(requestDto);

        assertEquals(responseDto, result);
    }

    @Test
    @DisplayName("Register_ifUserExist")
    void registerExist() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto("user@gmail.com", "password");
        UserEntity user = new UserEntity(1L, requestDto.getEmail(), "passwordEncode", UserRole.USER, null);

        when(userRepo.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(user));
        ApiException apiException = assertThrows(ApiException.class,
                () -> authService.register(requestDto));

        assertEquals(Exceptions.USER_EXIST, apiException.getExceptions());
    }

    @Test
    void login() {
        UserLoginRequest userLoginRequest = new UserLoginRequest("user@gmail.com", "password");
        UserEntity user = new UserEntity(1L, userLoginRequest.getEmail(), "password", UserRole.USER, null);
        String token = "token";
        LoginResponseDto responseDto = new LoginResponseDto(token);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userLoginRequest.getEmail(), userLoginRequest.getPassword());
        when(authenticationManager.authenticate(authenticationToken)).thenReturn(any());
        when(userRepo.findByEmail(userLoginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(token);
        doNothing().when(cartRepo).deleteByUserAndCartStatus(user, CartStatus.ADDED_TO_CART);

        LoginResponseDto result = authService.login(userLoginRequest);
        assertEquals(responseDto, result);
    }

    @Test
    void login_incorrectPassword() {
        UserLoginRequest userLoginRequest = new UserLoginRequest("user@gmail.com", "password1");

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userLoginRequest.getEmail(), userLoginRequest.getPassword());
        when(authenticationManager.authenticate(authenticationToken))
                .thenThrow(new BadCredentialsException("Message"));

        ApiException apiException = assertThrows(ApiException.class,
                () -> authService.login(userLoginRequest));
        assertEquals(Exceptions.USER_INCORRECT_PASSWORD, apiException.getExceptions());
    }

    @Test
    void login_userNotFound() {
        UserLoginRequest userLoginRequest = new UserLoginRequest("user11@gmail.com", "password");

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userLoginRequest.getEmail(), userLoginRequest.getPassword());
        when(authenticationManager.authenticate(authenticationToken)).thenReturn(any());

        ApiException apiException = assertThrows(ApiException.class,
                () -> authService.login(userLoginRequest));
        assertEquals(Exceptions.USER_NOT_FOUND, apiException.getExceptions());
    }

    @Test
    void getUserEntityByToken() {
        JwtService getJwtService = new JwtService();
        String email = "user@gmail.com";
        UserEntity user = new UserEntity(1L, email, "passwordEncode", UserRole.USER, null);
        String token = getJwtService.generateToken(user);
        String authHeader = "Bearer " + token;

        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        UserEntity result = authService.getUserEntityByToken(authHeader);
        assertEquals(user, result);
    }

    @Test
    void getUserEntityByToken_ifUserNotExist() {
        JwtService getJwtService = new JwtService();
        String email = "user@gmail.com";
        UserEntity user = new UserEntity(1L, email, "passwordEncode", UserRole.USER, null);
        String token = getJwtService.generateToken(user);
        String authHeader = "Bearer " + token;

        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class,
                () -> authService.getUserEntityByToken(authHeader));

        assertEquals(Exceptions.USER_NOT_FOUND, apiException.getExceptions());
    }
}