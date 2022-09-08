package com.elibrary.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.elibrary.model.repos.UserRepo;

public class EmailValidator implements ConstraintValidator<EmailValidation, String> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !userRepo.existsByEmail(value.toLowerCase());
    }

}
