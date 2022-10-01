package com.elibrary.model.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.elibrary.model.entity.BookRequest;

public interface BookRequestRepo extends JpaRepository<BookRequest, Long>{

     @Query(nativeQuery = true, value = "SELECT * FROM book_requests WHERE lower(title) LIKE lower(concat('%', ?1, '%')) AND available = ?2")
     Page<BookRequest> findByTitleContainingIgnoreCase(String search, boolean available, Pageable pageable);
    
     @Query(nativeQuery = true, value = "SELECT * FROM book_requests WHERE lower(title) LIKE lower(concat('%', ?1, '%')) AND user_id = ?2 AND available = ?3")
     Page<BookRequest> findByTitleContainingIgnoreCaseMember(String search, long id, boolean available, Pageable pageable);

     boolean existsById(long id);

}
