package com.elibrary.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elibrary.model.entity.Borrow;

public interface BorrowRepo extends JpaRepository<Borrow, Long> {
    
}
