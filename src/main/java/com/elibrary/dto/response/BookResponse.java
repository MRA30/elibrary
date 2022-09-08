package com.elibrary.dto.response;

import com.elibrary.model.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {
    
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private String yearPublication;
    private Integer quantity;
    private Category category;
    private String image;

}
