package com.elibrary.dto.request;

import com.elibrary.validators.BookIdValidation;
import com.elibrary.validators.UserIdValidation;
import lombok.Data;

@Data
public class BorrowRequestAdd {
    
    private Long id;

    @BookIdValidation(message = "Book does not exist")
    private Long bookId;

    @UserIdValidation(message = "User does not exist")
    private long userId;

    private String description;
}
