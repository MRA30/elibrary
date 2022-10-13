package com.elibrary.dto.request;

import com.elibrary.validators.BookIdValidation;
import com.elibrary.validators.UserIdValidation;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class BorrowRequestAdd {
    
    private Long id;

    @NotNull(message = "Book id must not be null")
    private long bookId;

    @NotNull(message = "User id is required")
    private long userId;

    private String description;
}
