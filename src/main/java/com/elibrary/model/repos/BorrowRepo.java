package com.elibrary.model.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elibrary.model.entity.Borrow;

public interface BorrowRepo extends JpaRepository<Borrow, Long> {
    
    Optional<Borrow> findById(long id);

}
