package com.elibrary.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.elibrary.model.repos.CategoryRepo;

public class CategoryNameValidator implements ConstraintValidator<CategoryNameValidation, String> {

    @Autowired
    private CategoryRepo categoryRepo;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !categoryRepo.existsByCategory(value);
    }

}
