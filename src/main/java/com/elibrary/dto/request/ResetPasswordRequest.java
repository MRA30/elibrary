package com.elibrary.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    @Min(value = 8, message = "Password must be at least 8 characters")
    @Max(value = 20, message = "Password must be at most 20 characters")
    private String password;
}
