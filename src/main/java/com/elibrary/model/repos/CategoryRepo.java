package com.elibrary.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elibrary.model.entity.Category;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    boolean existsByCategory(String category);
    boolean existsById(long id);
}
