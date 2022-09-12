package com.elibrary.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.elibrary.model.repos.UserRepo;

public class UserIdValidator implements ConstraintValidator<UserIdValidation, Long>{

    @Autowired
    private UserRepo userRepo;

    @Override
    public boolean isValid(Long id, ConstraintValidatorContext context) {
        return userRepo.existsById(id);
    }

}
