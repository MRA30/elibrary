package com.elibrary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowOvertimeResponse {

    private long id;
    private long bookId;
    private String bookTitle;
    private long userId;
    private String fullName;
    private Date borrowDate;
    private int daysOvertime;
    private String description;
}
