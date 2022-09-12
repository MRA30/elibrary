package com.elibrary.dto.request;

import java.sql.Date;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.elibrary.validators.BookIdValidation;
import com.elibrary.validators.UserIdValidation;

import lombok.Data;

@Data
public class BorrowRequest {
    
    private Long id;
    
    @BookIdValidation(message = "Book does not exist")
    @NotNull(message = "Book id must not be empty")
    private Long bookId;

    @UserIdValidation(message = "User does not exist")
    @NotNull(message = "User id must not be empty")
    private Long userId;

    @NotEmpty(message = "Borrow Date must not be empty")
    private Date borrowDate;

    private Date returnDate;
    private boolean isReturned;
    private double penalty;
    private boolean isBrokenorLost;
    private String description;
}
