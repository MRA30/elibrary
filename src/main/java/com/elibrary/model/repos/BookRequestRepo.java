package com.elibrary.model.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.elibrary.dto.response.BookRequestResponse;
import com.elibrary.model.entity.BookRequest;

public interface BookRequestRepo extends JpaRepository<BookRequest, Long>, JpaSpecificationExecutor<BookRequest>{

    Page<BookRequest> findAll(Pageable pageable);

    @Query("SELECT new com.elibrary.dto.response.BookRequestReponse "+ 
    "(br.id, br.userId, u.name, br.title, br.isAvailable, br.description) "+
    "FROM User u RIGHT JOIN BookRequest br ON u.id = br.userId WHERE lower(br.title) LIKE %?1%")
    Page<BookRequestResponse> findByTitleContainingIgnoreCase(String search, Pageable pageable);
    
    @Query("SELECT new com.elibrary.dto.response.BookRequestReponse "+ 
    "(br.id, br.userId, u.name, br.title, br.isAvailable, br.description) "+
    "FROM User u RIGHT JOIN BookRequest br ON u.id = br.userId WHERE lower(br.title) "+
    "LIKE %?1% AND br.userId=?2")
    Page<BookRequestResponse> findByTitleContainingIgnoreCaseMember(String search, long id, Pageable pageable);

    @Query("SELECT new com.elibrary.dto.response.BookRequestReponse "+ 
    "(br.id, br.userId, u.name, br.title, br.isAvailable, br.description) "+
    "FROM User u RIGHT JOIN BookRequest br ON u.id = br.userId WHERE br.id =?1")
    BookRequestResponse findByIdBookRequest(long id);
}
