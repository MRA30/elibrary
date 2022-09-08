package com.elibrary.dto.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdatePasswordRequest {

    @NotEmpty(message = "Old password is required")
    private String oldPassword;

    @NotEmpty(message = "New password is required")
    @Size(min = 8, max = 20, message = "Password must be in 6 to 20 characters")
    private String newPassword;

}
