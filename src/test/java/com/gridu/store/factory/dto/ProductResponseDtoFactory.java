package com.gridu.store.factory.dto;

import com.gridu.store.dto.response.ProductResponseDto;
import java.util.List;

public class ProductResponseDtoFactory {

    public static List<ProductResponseDto> createProductResponseDTOs() {
     return List.of(
                new ProductResponseDto(1L, "phone", 10L, 2000),
                new ProductResponseDto(2L, "phone2", 10L, 3000),
                new ProductResponseDto(3L, "phone3", 10L, 4000)
        );
    }
}
