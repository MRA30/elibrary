package com.elibrary.controllers;

import com.elibrary.Constans;
import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.CategoryException;
import com.elibrary.dto.request.CategoryRequest;
import com.elibrary.dto.response.CategoryResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/employee/add")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<CategoryResponse>> addCategory(@Valid @RequestBody CategoryRequest request) throws CategoryException {
        Map<String, String> messagesList = new HashMap<>();
        CategoryResponse categoryResponse = categoryService.addCategory(request);
        messagesList.put(Constans.MESSAGE, "Category added successfully");
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(true, messagesList, categoryResponse));
    }

    @PutMapping("/employee/update/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<CategoryResponse>> updateCategory(@Valid @PathVariable("id") Long id, @Valid @RequestBody CategoryRequest request) throws BusinessNotFound, CategoryException {
        Map<String, String> messagesList = new HashMap<>();
            CategoryResponse categoryResponse = categoryService.updateCategory(id, request);
            messagesList.put(Constans.MESSAGE, "Category updated successfully");
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(true, messagesList, categoryResponse));
    }

    @GetMapping("/employee")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<List<CategoryResponse>>> getAllCategories(@RequestParam(defaultValue = "") String search){
        Map<String, String> messagesList = new HashMap<>();
        try {
            messagesList.put(Constans.MESSAGE, "Categories fetched successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, categoryService.findAll(search.toLowerCase())));
        }catch (Exception e){
            messagesList.put(Constans.MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @DeleteMapping("employee/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<CategoryResponse>> deleteCategory(@PathVariable("id") long id) throws BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
            categoryService.delete(id);
            messagesList.put(Constans.MESSAGE, "Category deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(true, messagesList, null));
    }
    
}
