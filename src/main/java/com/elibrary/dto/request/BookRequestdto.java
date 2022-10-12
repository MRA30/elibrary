package com.elibrary.dto.request;

import com.elibrary.validators.CategoryIdValidation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestdto {
    
    private Long id;

    @NotEmpty(message = "Book Title must not be empty")
    private String title;

    @NotEmpty(message = "author must not be empty")
    private String author;

    @NotEmpty(message = "publisher must not be empty")
    private String publisher;

    @NotEmpty(message = "year publication must not be empty")
    private String yearPublication;

    @NotNull(message = "quantity must not be empty")
    private Integer quantity;

    @NotNull(message = "category must not be empty")
    private Long category;

    private String synopsis;
}
