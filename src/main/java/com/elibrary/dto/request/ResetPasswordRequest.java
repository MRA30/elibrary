package com.elibrary.dto.request;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    @Size(min = 8, max = 20, message = "Password must be at least 8 characters and less than 20 characters")
    private String password;
}
