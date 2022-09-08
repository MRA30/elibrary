package com.elibrary.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elibrary.dto.request.CategoryRequest;
import com.elibrary.dto.response.CategoryResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.CategoryService;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/add")
    public ResponseEntity<ResponseData<CategoryResponse>> addCategory(@Valid @RequestBody CategoryRequest request, Errors errors){
        ResponseData<CategoryResponse> response = new ResponseData<>();
        if(errors.hasErrors()){
            for (ObjectError error : errors.getAllErrors()) {
                response.getMessages().add(error.getDefaultMessage());
            }
            response.setStatus(false);
            response.setPayload(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try{
            CategoryResponse categoryResponse = categoryService.addCategory(request);
            response.setStatus(true);
            response.setPayload(categoryResponse);
            response.getMessages().add("Category added successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch(Exception e){
            response.setStatus(false);
            response.setPayload(null);
            response.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<CategoryResponse>> updateCategory(@Valid @PathVariable("id") Long id, @Valid @RequestBody CategoryRequest request, Errors errors){
        ResponseData<CategoryResponse> response = new ResponseData<>();
        if(errors.hasErrors()){
            for (ObjectError error : errors.getAllErrors()) {
                response.getMessages().add(error.getDefaultMessage());
            }
            response.setStatus(false);
            response.setPayload(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try{
            CategoryResponse categoryResponse = categoryService.updateCategory(id,request);
            response.setStatus(true);
            response.setPayload(categoryResponse);
            response.getMessages().add("Category updated successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch(Exception e){
            response.setStatus(false);
            response.setPayload(null);
            response.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<CategoryResponse>> deleteCategory(@PathVariable("id") long id){
        ResponseData<CategoryResponse> response = new ResponseData<>();
        try{
            categoryService.delete(id);
            response.setStatus(true);
            response.setPayload(null);
            response.getMessages().add("Category deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch(Exception e){
            response.setStatus(false);
            response.setPayload(null);
            response.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
}
