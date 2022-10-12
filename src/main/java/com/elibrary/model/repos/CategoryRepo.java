package com.elibrary.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elibrary.model.entity.Category;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    boolean existsByCategory(String category);

    boolean existsById(long id);

    Category findByCategory(String category);

    Optional<Category> findById(long id);
}
