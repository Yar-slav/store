package com.gridu.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductForCartResponse {
    private Long numberOfProduct;
    private String title;
    private double price;
    private Long quantities;
    private double subtotalPrice;
}
