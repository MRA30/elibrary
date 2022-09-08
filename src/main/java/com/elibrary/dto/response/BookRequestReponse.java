package com.elibrary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestReponse {
    
    private Long id;
    private Long userId;
    private String title;
    private boolean isAvailable;
    private String description;
}
