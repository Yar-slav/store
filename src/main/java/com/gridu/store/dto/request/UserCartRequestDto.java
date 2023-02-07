package com.gridu.store.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCartRequestDto {
    private Long id;

    @Min(value = 1, message = "Quantity should be one or greater")
    private Long quantity;
}
