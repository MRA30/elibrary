package com.elibrary.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import com.elibrary.validators.EmailValidation;
import com.elibrary.validators.GenderValidation;
import com.elibrary.validators.NoHpValidation;
import com.elibrary.validators.UsernameValidation;

import lombok.Data;

@Data
public class RegisterMemberRequest {

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
    private String password;
}
