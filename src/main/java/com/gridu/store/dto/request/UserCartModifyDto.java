package com.gridu.store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCartModifyDto {

    @JsonProperty("id")
    private Long productId;

    @Min(value = 1, message = "Quantity should be one or greater")
    private Long quantity;
}
