package com.elibrary.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.elibrary.model.repos.UserRepo;

public class UsernameValidator implements ConstraintValidator<UsernameValidation, String> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        return !userRepo.existsByUsername(username);
    }

}
