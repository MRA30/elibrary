package com.elibrary.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elibrary.model.repos.CategoryRepo;

public class CategoryIdValidator implements ConstraintValidator<CategoryIdValidation, Long>{

    private CategoryRepo categoryRepo;

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        return categoryRepo.existsById(value);
    }

}
