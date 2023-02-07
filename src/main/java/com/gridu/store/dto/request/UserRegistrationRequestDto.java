package com.gridu.store.dto.request;

import com.gridu.store.lib.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequestDto {

    @ValidEmail
    private String email;

    @NotBlank(message = "Password can't be null or whitespace")
    private String password;
}
