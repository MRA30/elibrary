package com.elibrary.dto.request;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class BookRequestRequest {

    private Long id;

    @NotEmpty(message = "Book Title must not be empty")
    private String title;

    @NotEmpty(message = "userId must not be empty")
    private Long userId;
    
    private boolean isAvailable;
    private String description;
    
}
