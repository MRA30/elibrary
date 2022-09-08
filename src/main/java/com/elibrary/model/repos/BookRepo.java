package com.elibrary.model.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.elibrary.model.entity.Book;

public interface BookRepo extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book>{

    boolean existsByTitle(String title);

    Book findByTitle(String title);
    
    Page<Book> findAll(Pageable pageable);
}
