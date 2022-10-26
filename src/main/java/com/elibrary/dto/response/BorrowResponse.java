package com.elibrary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowResponse {
    
    private Long id;

    private Long bookId;

    private String bookTitle;

    private Long userId;

    private String fullName;

    private String borrowDate;

    private String returnDate;

    private boolean isReturned;

    private double penalty;

    private String description;
}
