package com.elibrary.dto.request;

import javax.validation.constraints.NotEmpty;

import com.elibrary.validators.CategoryNameValidation;

import lombok.Data;

@Data
public class CategoryRequest {
    
    private Long id;
    
    @NotEmpty(message = "Category Name must not be empty")
    private String category;
}
