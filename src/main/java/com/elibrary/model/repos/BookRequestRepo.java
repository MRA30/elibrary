package com.elibrary.model.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.elibrary.model.entity.BookRequest;

public interface BookRequestRepo extends JpaRepository<BookRequest, Long>, JpaSpecificationExecutor<BookRequest>{

    Page<BookRequest> findAll(Pageable pageable);

}
