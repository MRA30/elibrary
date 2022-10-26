package com.elibrary.dto.request;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BorrowRequestUpdate {

    private boolean lostOrDamage;

    private boolean returned;

    private double penalty;

    private String description;
}
