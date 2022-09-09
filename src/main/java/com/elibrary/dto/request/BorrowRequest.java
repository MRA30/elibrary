package com.elibrary.dto.request;

import java.sql.Date;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class BorrowRequest {
    
    private Long id;
    
    @NotNull(message = "Book id must not be empty")
    private Long bookId;

    @NotNull(message = "User id must not be empty")
    private Long userId;

    @NotEmpty(message = "Borrow Date must not be empty")
    private Date borrowDate;

    private Date returnDate;
    private boolean isReturned;
    private double penalty;
    private String description;
}
