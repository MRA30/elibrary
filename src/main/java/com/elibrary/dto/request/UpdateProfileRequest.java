package com.elibrary.dto.request;

import com.elibrary.validators.GenderValidation;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotEmpty(message = "username cannot be empty")
    private String username;

    @NotEmpty(message = "first name cannot be empty")
    private String firstName;

    @NotEmpty(message = "last name cannot be empty")
    private String lastName;

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
