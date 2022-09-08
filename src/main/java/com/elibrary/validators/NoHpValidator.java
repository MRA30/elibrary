package com.elibrary.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.elibrary.model.repos.UserRepo;

public class NoHpValidator implements ConstraintValidator<NoHpValidation, String> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !userRepo.existsBynoHp(value);
    }

}
