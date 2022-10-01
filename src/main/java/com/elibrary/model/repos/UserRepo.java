package com.elibrary.model.repos;

import com.elibrary.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    User findByEmail(String email);
    User findByNumberIdentity(String numberIdentity);

    User findBynoHp(String noHp);

    boolean existsByEmail(String email);

    boolean existsByNumberIdentity(String numberIdentity);

    boolean existsBynoHp(String noHp);

    boolean existsById(long id);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndPassword(String username, String password);

    Page<User> findAll(Specification<User> and, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userRole = ?2 AND (lower(CONCAT(u.firstName,' ',u.lastName )) LIKE %?1% OR lower(u.username) LIKE %?1% OR lower(u.numberIdentity) LIKE %?1%)")
    Page<User> findAllUsers(String search, String userRole, Pageable pageable);

    @Query("SELECT u FROM User u WHERE lower(CONCAT(u.firstName,' ',u.lastName )) LIKE %?1% OR lower(u.username) LIKE %?1% OR lower(u.numberIdentity) LIKE %?1%")
    List<User> findAllWithoutPaging(String search);
}
