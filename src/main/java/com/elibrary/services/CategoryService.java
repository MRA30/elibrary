package com.elibrary.services;

import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.CategoryException;
import com.elibrary.dto.request.CategoryRequest;
import com.elibrary.dto.response.CategoryResponse;
import com.elibrary.model.entity.Category;
import com.elibrary.model.repos.CategoryRepo;
import com.elibrary.model.specification.CategorySpecification;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
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
    
    public CategoryResponse addCategory(CategoryRequest request) throws CategoryException {
        if(categoryRepo.existsByCategory(request.getCategory())){
            throw new CategoryException("Category already exists");
        }
        Category category = new Category();
        category.setCategory(request.getCategory());
        categoryRepo.save(category);
        return convertCategoryToResponse(category);
    }

    public CategoryResponse updateCategory(Long id,CategoryRequest request) throws BusinessNotFound, CategoryException {
        Optional<Category> category = categoryRepo.findById(id);
        if(category.isPresent()){
            Category category1 = category.get();
            if(categoryRepo.existsByCategory(request.getCategory()) && !Objects.equals(categoryRepo.findByCategory(request.getCategory()).getId(), id)){
                throw new CategoryException("Category already exists");
            }
            category1.setCategory(request.getCategory());
            categoryRepo.save(category1);
            return convertCategoryToResponse(category1);
        }else {
            throw new BusinessNotFound("Category not found");
        }
    }

    public List<CategoryResponse> findAll(String search){
        List<Category> categories = categoryRepo.findAll(Specification.where(categorySpecification.searchCategory(search)));
        return categories.stream().map(category -> new CategoryResponse(
                category.getId(),
                category.getCategory()
        )).collect(Collectors.toList());
    }

    public void delete(long id) throws BusinessNotFound {
        Optional<Category> category = categoryRepo.findById(id);
        if (category.isPresent()){
            categoryRepo.deleteById(id);
        }else {
            throw new BusinessNotFound("Category not found");
        }

    }

}
