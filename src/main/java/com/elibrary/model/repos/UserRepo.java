package com.elibrary.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elibrary.model.entity.User;

public interface UserRepo extends JpaRepository<User, Long> {
    
    User findByEmail(String email);
    
    User findByNumberIdentity(String numberIdentity);

    User findBynoHp(String noHp);

    User findByImage(String image);

    boolean existsByEmail(String email);

    boolean existsByNumberIdentity(String numberIdentity);

    boolean existsBynoHp(String noHp);

    boolean existsById(long id);

}
