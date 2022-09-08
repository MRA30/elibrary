package com.elibrary.services;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.elibrary.dto.request.CategoryRequest;
import com.elibrary.dto.response.CategoryResponse;
import com.elibrary.model.entity.Category;
import com.elibrary.model.repos.CategoryRepo;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    public Category convertCategoryResponseToCategory(CategoryResponse response){
        Category category = new Category();
        category.setId(response.getId());
        category.setCategory(response.getCategory());
        return category;
    }

    public CategoryResponse convertCategoryToResponse(Category category){
        CategoryResponse response = new CategoryResponse(
            category.getId(),
            category.getCategory()
        );
        return response;
    }

    public CategoryResponse findById(long id){
        Optional<Category> category = categoryRepo.findById(id);
        if(category.isPresent()){
            CategoryResponse response = new CategoryResponse(
                category.get().getId(),
                category.get().getCategory()
            );
            return response;
        }
        return null;
    }
    
    public CategoryResponse addCategory(CategoryRequest request){
        Category category = new Category();
        category.setCategory(request.getCategory());
        categoryRepo.save(category);
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setCategory(category.getCategory());
        return response;
    }

    public CategoryResponse updateCategory(Long id,CategoryRequest request){
        CategoryResponse categoryResponse = findById(id);
        Category category =  new Category();
        category.setId(categoryResponse.getId());
        category.setCategory(request.getCategory());
        categoryRepo.save(category);

        CategoryResponse response = new CategoryResponse(
                                        category.getId(), 
                                        category.getCategory());
        return response;
    }

    public void delete(long id){
        categoryRepo.deleteById(id);
    }
}
