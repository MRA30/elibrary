package com.elibrary.dto.response;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowResponse {
    
    private Long id;
    private Long bookId;
    private Long userId;
    private Date borrowDate;
    private Date returnDate;
    private boolean isReturned;
    private double penalty;
    private String description;
}
