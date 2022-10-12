package com.elibrary.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.elibrary.validators.EmailValidation;
import com.elibrary.validators.GenderValidation;
import com.elibrary.validators.NoHpValidation;
import com.elibrary.validators.UsernameValidation;

import lombok.Data;

@Data
public class RegisterMemberRequest {

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

    @Size(min = 8, message = "password must be at least 8 character")
    @Size(max = 16, message = "password must be at most 16 character")
    private String password;
}
