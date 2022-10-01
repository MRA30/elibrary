package com.elibrary.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elibrary.model.entity.Category;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryRepo extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    boolean existsByCategory(String category);

    boolean existsById(long id);
}
