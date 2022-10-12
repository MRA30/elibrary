package com.elibrary.dto.request;

import com.elibrary.validators.*;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class RegisterEmployeeRequest {
    
    @NotEmpty(message = "number identity cannot be empty")
    @Size(min = 11, max = 11, message = "number identity must be 11 character")
    private String numberIdentity;

    @NotEmpty(message = "username cannot be empty")
    private String username;

    @NotEmpty(message = "first name cannot be empty")
    private String firstName;

    @NotEmpty(message = "last name cannot be empty")
    private String lastName;

    @NotEmpty(message = "gender cannot be empty")
    @GenderValidation
    private String gender;

    @NotEmpty(message = "no hp cannot be empty")
    private String noHp;

    @NotEmpty(message = "address cannot be empty")
    private String address;

    @NotEmpty(message = "email cannot be empty")
    @Email(message = "email must be valid")
    private String email;

    @NotEmpty(message = "password cannot be empty")
    @Size(min = 8, message = "password must be at least 8 character")
    private String password;

}
