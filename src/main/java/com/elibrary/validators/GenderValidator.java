package com.elibrary.validators;

import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class GenderValidator implements ConstraintValidator<GenderValidation, String> {

    public boolean isValid(String value, ConstraintValidatorContext cxt) {
        List<String> list = Arrays.asList(new String[]{"Male", "Female"});
        return list.contains(value);
    }
    
}
