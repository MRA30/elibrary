package com.elibrary.dto.request;

import javax.validation.constraints.NotEmpty;

import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotEmpty(message = "Old password is required")
    private String oldPassword;

    @NotEmpty(message = "New password is required")
    @Size(min = 8, max = 20, message = "Password must be at least 8 characters and less than 20 characters")
    private String newPassword;

}
