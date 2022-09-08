package com.elibrary.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import com.elibrary.validators.EmailValidation;
import com.elibrary.validators.GenderValidation;
import com.elibrary.validators.NoHpValidation;

import lombok.Data;

@Data
public class RegisterMemberRequest {

    @NotEmpty(message = "name cannot be empty")
    private String name;

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

    private String image;
}
