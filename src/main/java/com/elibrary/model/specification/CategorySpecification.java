package com.elibrary.model.specification;

import com.elibrary.model.entity.Category;
import com.elibrary.model.entity.Category_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CategorySpecification {

    public Specification<Category> searchCategory(String search) {
        return ((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(Category_.CATEGORY)), "%" + search.toLowerCase() + "%"));
    }
}
