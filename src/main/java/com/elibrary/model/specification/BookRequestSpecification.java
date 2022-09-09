package com.elibrary.model.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.elibrary.model.entity.BookRequest;
import com.elibrary.model.entity.BookRequest_;

@Component
public class BookRequestSpecification {
 
    public Specification<BookRequest> filterByTitle(String search) {
        return ((root, criteriaQuery, criteriaBuilder) ->{
            return criteriaBuilder.like(criteriaBuilder.lower(root.get(BookRequest_.TITLE)), "%" + search.toLowerCase() + "%");
        }); 
    }

    public Specification<BookRequest> filterByUserId(Long userId) {
        return ((root, criteriaQuery, criteriaBuilder) ->{
            return criteriaBuilder.equal(root.get(BookRequest_.USER_ID), userId);
        }); 
    }
    
    public Specification<BookRequest> searchByTitleAndFilterByUserId(String search, Long userId){
            return (Specification.where(filterByTitle(search)).and(filterByUserId(userId)));
    }

}
