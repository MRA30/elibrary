package com.elibrary.dto.request;

import com.elibrary.validators.BookIdValidation;
import com.elibrary.validators.UserIdValidation;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class BorrowRequestAdd {
    
    private Long id;

    @Size(min = 1, message = "Book must be selected")
    private long bookId;

    @Size(min = 1, message = "User must be selected")
    private long userId;

    private String description;
}
