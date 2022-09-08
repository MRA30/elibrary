package com.elibrary.dto.response;

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
    private String borrowDate;
    private String returnDate;
    private String isReturned;
    private double penalty;
    private String description;
}
