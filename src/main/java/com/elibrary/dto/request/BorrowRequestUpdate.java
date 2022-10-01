package com.elibrary.dto.request;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BorrowRequestUpdate {
    private boolean isLostBroken;
    private boolean isReturned;
    private double penalty;
    private String description;
}
