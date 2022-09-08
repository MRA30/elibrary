package com.elibrary.dto.request;

import javax.validation.constraints.NotEmpty;

import com.elibrary.validators.GenderValidation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotEmpty(message = "name is required")
    private String name;

    @GenderValidation
    @NotEmpty(message = "gender is required")
    private String Gender;
    
    @NotEmpty(message = "no hp is required")
    private String noHp;

    @NotEmpty(message = "address is required")
    private String address;

    @NotEmpty(message = "email is required")
    private String email;
    
}
