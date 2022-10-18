package com.elibrary.dto.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class UpdatePasswordRequest {

    @NotEmpty(message = "Old password is required")
    private String oldPassword;

    @Min(value = 8, message = "Password must be at least 8 characters")
    @Max(value = 20, message = "Password must be less than 20 characters")
    private String newPassword;

}
