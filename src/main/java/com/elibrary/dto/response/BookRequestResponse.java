package com.elibrary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestResponse {
    
    private Long id;
    private Long userId;
    private String name;
    private String title;
    private boolean isAvailable;
    private String description;
}
