package com.elibrary.dto.request;

import java.sql.Date;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class BorrowRequest {
    
    private Long id;
    
    @NotEmpty(message = "Book id must not be empty")
    private Long bookId;

    @NotEmpty(message = "User id must not be empty")
    private Long userId;

    @NotEmpty(message = "Borrow Date must not be empty")
    private Date borrowDate;

    private Date returnDate;
    private boolean isReturned;
    private double penalty;
    private String description;
}
