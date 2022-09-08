package com.elibrary.model.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.Book_;

@Component
public class BookSpecification {

    public Specification<Book> filterByTitle(String search) {
        return ((root, criteriaQuery, criteriaBuilder) ->{
            return criteriaBuilder.like(root.get(Book_.TITLE.toLowerCase()), "%" + search.toLowerCase() + "%");
        }); 
    }
    public Specification<Book> filterByAuthor(String search) {
        return ((root, criteriaQuery, criteriaBuilder) ->{
            return criteriaBuilder.like(root.get(Book_.AUTHOR.toLowerCase()), "%" + search.toLowerCase() + "%");
        }); 
    }
    public Specification<Book> filterByPublisher(String search) {
        return ((root, criteriaQuery, criteriaBuilder) ->{
            return criteriaBuilder.like(root.get(Book_.PUBLISHER.toLowerCase()), "%" + search.toLowerCase() + "%");
        }); 
    }

    public Specification<Book> searchBook(String search){
        return (Specification.where(filterByTitle(search))
                            .or(filterByAuthor(search))
                            .or(filterByPublisher(search)));
    }
    
}
