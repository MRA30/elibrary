package com.elibrary.dto.request;

import javax.validation.constraints.NotEmpty;

import com.elibrary.validators.CategoryIdValidation;

import lombok.Data;

@Data
public class BookRequest {
    
    private Long id;

    @NotEmpty(message = "Book Title must not be empty")
    private String title;

    @NotEmpty(message = "author must not be empty")
    private String author;

    @NotEmpty(message = "publisher must not be empty")
    private String publisher;

    @NotEmpty(message = "year publication must not be empty")
    private String yearPublication;

    @NotEmpty(message = "quantity must not be empty")
    private Integer quantity;

    @NotEmpty(message = "category must not be empty")
    @CategoryIdValidation(message = "Category does not exist")
    private Long category;
}
