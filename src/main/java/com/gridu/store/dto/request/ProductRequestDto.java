package com.gridu.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {

    @NotBlank(message = "Title can't be null or whitespace")
    private String title;

    @Min(value = 1, message = "Quantity should be one or greater")
    private Long quantity;

    @Min(value = 0, message = "Price should be zero or greater")
    private double price;
}
