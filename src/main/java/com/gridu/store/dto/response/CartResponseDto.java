package com.gridu.store.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
//@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDto {
    private List<ProductForCartResponse> products;
    private double totalPrice;
}
