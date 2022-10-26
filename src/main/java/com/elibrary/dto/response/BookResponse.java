package com.elibrary.dto.response;

import com.elibrary.model.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private long idCategory;

    private String category;

    private String synopsis;

    private List<String> images;
}
