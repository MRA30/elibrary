package com.elibrary.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.elibrary.model.repos.BookRepo;

public class BookIdValidator implements ConstraintValidator<BookIdValidation, Long>{

    @Autowired
    private BookRepo bookRepo;

    @Override
    public boolean isValid(Long id, ConstraintValidatorContext context) {
        return bookRepo.existsById(id);
    }

}
