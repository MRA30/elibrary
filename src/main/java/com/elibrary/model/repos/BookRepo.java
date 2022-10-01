package com.elibrary.model.repos;

import com.elibrary.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookRepo extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book>{

    Optional<Book> findById(Long id);

    boolean existsByTitle(String title);

    boolean existsById(long id);

    Book findByTitle(String title);

    @Query("SELECT b FROM Book b WHERE b.category.id = ?1 AND (lower(b.title) LIKE %?2% OR lower(b.author) LIKE %?2% OR lower(b.publisher) LIKE %?2%)")
    Page<Book> findBooksByCategory(long id, String search, Pageable pageable);
}
