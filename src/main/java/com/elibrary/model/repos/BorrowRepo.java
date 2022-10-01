package com.elibrary.model.repos;

import com.elibrary.model.entity.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BorrowRepo extends JpaRepository<Borrow, Long> {

    Borrow findById(long id);

    boolean existsById(long id);

    @Query(nativeQuery = true,value = "SELECT COUNT(*) FROM borrows WHERE book_id = ?1 AND is_returned = false")
    int countBookBorrow(long id);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM borrows WHERE book_id = ?1 AND user_id = ?2 AND is_returned = false")
    int checkBorrow(long bookId, long userId);

    @Query(nativeQuery = true, value = "SELECT * FROM borrows WHERE user_id = ?1 AND is_returned = ?2")
    Page<Borrow> filterBorrowsByUserId(long userId, boolean returnStatus, Pageable pageable);

    @Query("SELECT b FROM Borrow b WHERE b.isReturned = ?2 AND (lower(b.book.title) LIKE %?1% OR lower(concat(b.user.firstName,' ',b.user.lastName)) LIKE %?1% OR lower(b.user.numberIdentity) LIKE %?1%)")
    Page<Borrow> findAllBorrows(String search, boolean returned, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM borrows WHERE is_returned = false AND DATE_PART('day', now() - borrow_date) > 7")
    Page<Borrow> findAllBorrowsOvertime(Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM borrows WHERE user_id = ?1 AND is_returned = false")
    List<Borrow> findBorrowsByUserId(long id);

}
