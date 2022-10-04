package com.elibrary.controllers;

import com.elibrary.dto.request.CategoryRequest;
import com.elibrary.dto.response.CategoryResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/employee/add")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<CategoryResponse>> addCategory(@Valid @RequestBody CategoryRequest request, Errors errors){
        List<String> messagesList = new ArrayList<>();
        if(errors.hasErrors()){
            for (ObjectError error : errors.getAllErrors()) {
                messagesList.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false,messagesList, null));
        }
        CategoryResponse categoryResponse = categoryService.addCategory(request);
        messagesList.add("Category added successfully");
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(true,messagesList, categoryResponse));
    }

    @PutMapping("/employee/update/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<CategoryResponse>> updateCategory(@Valid @PathVariable("id") Long id, @Valid @RequestBody CategoryRequest request, Errors errors){
        List<String> messagesList = new ArrayList<>();
        if(!categoryService.existsById(id)){
            messagesList.add("Category not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false,messagesList, null));
        }
        if(errors.hasErrors()){
            for (ObjectError error : errors.getAllErrors()) {
                messagesList.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false,messagesList, null));
        }
        CategoryResponse categoryResponse = categoryService.updateCategory(id,request);
        messagesList.add("Category updated successfully");
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(true,messagesList, categoryResponse));
    }

    @GetMapping("/employee")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<List<CategoryResponse>>> getAllCategories(@RequestParam(defaultValue = "") String search){
        List<String> messagesList = new ArrayList<>();
        messagesList.add("Categories fetched successfully");
        return ResponseEntity.ok(new ResponseData<>(true,messagesList, categoryService.findAll(search.toLowerCase())));
    }

    @DeleteMapping("employee/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<CategoryResponse>> deleteCategory(@PathVariable("id") long id){
        List<String> messagesList = new ArrayList<>();
        if(!categoryService.existsById(id)){
            messagesList.add("Category not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false,messagesList, null));
        }
        categoryService.delete(id);
        messagesList.add("Category deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(true,messagesList, null));
    }
    
}
