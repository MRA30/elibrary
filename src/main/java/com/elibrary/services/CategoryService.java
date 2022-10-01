package com.elibrary.services;

import com.elibrary.dto.request.CategoryRequest;
import com.elibrary.dto.response.CategoryResponse;
import com.elibrary.model.entity.Category;
import com.elibrary.model.repos.CategoryRepo;
import com.elibrary.model.specification.CategorySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private CategorySpecification categorySpecification;

    public Category convertCategoryResponseToCategory(CategoryResponse response){
        Category category = new Category();
        category.setId(response.getId());
        category.setCategory(response.getCategory());
        return category;
    }

    public CategoryResponse convertCategoryToResponse(Category category){
        return new CategoryResponse(
            category.getId(),
            category.getCategory()
        );
    }

    public boolean existsById(long id){
        return categoryRepo.existsById(id);
    }

    public CategoryResponse findById(long id){
        Optional<Category> category = categoryRepo.findById(id);
        return category.map(this::convertCategoryToResponse).orElse(null);
    }
    
    public CategoryResponse addCategory(CategoryRequest request){
        Category category = new Category();
        category.setCategory(request.getCategory());
        categoryRepo.save(category);
        return convertCategoryToResponse(category);
    }

    public CategoryResponse updateCategory(Long id,CategoryRequest request){
        CategoryResponse categoryResponse = findById(id);
        Category category =  new Category();
        category.setId(categoryResponse.getId());
        category.setCategory(request.getCategory());
        categoryRepo.save(category);
        return convertCategoryToResponse(category);
    }

    public List<CategoryResponse> findAll(String search){
        List<Category> categories = categoryRepo.findAll(Specification.where(categorySpecification.searchCategory(search)));
        return categories.stream().map(category -> new CategoryResponse(
                category.getId(),
                category.getCategory()
        )).collect(Collectors.toList());
    }

    public void delete(long id){
        categoryRepo.deleteById(id);
    }

}
