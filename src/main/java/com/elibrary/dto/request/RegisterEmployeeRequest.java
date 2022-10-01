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
    @NumberIdentityValidation(message = "number identity already exist")
    private String numberIdentity;

    @NotEmpty(message = "username cannot be empty")
    @UsernameValidation(message = "username already exist")
    private String username;

    @NotEmpty(message = "first name cannot be empty")
    private String firstName;

    @NotEmpty(message = "last name cannot be empty")
    private String lastName;

    @GenderValidation
    @NotEmpty(message = "gender cannot be empty")
    private String gender;

    @NotEmpty(message = "no hp cannot be empty")
    @NoHpValidation(message = "no hp already exist")
    private String noHp;

    @NotEmpty(message = "address cannot be empty")
    private String address;

    @NotEmpty(message = "email cannot be empty")
    @Email(message = "email must be valid")
    @EmailValidation(message = "email already exist")
    private String email;

    @NotEmpty(message = "password cannot be empty")
    @Size(min = 8,  max = 20, message = "password lenght must be in 8 to 20 character")
    private String password;

}
