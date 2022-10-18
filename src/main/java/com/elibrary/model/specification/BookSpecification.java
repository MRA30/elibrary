package com.elibrary.model.specification;

import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.Book_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class BookSpecification {

    public Specification<Book> filterByTitle(String search) {
        return ((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(Book_.TITLE)), "%" + search.toLowerCase() + "%"));
    }
    public Specification<Book> filterByAuthor(String search) {
        return ((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(Book_.AUTHOR)), "%" + search.toLowerCase() + "%"));
    }
    public Specification<Book> filterByPublisher(String search) {
        return ((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(Book_.PUBLISHER)), "%" + search.toLowerCase() + "%"));
    }

    public Specification<Book> searchBook(String search) {
        return (Specification.where(filterByTitle(search))
                            .or(filterByAuthor(search))
                            .or(filterByPublisher(search)));
    }
    
}
