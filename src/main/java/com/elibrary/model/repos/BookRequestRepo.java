package com.elibrary.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elibrary.model.entity.BookRequest;

public interface BookRequestRepo extends JpaRepository<BookRequest, Long> {
}
