package com.elibrary.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elibrary.dto.request.BorrowRequest;
import com.elibrary.dto.response.BorrowResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.BorrowService;

@RestController
@RequestMapping("/borrows")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @PostMapping("/add")
    public ResponseEntity<ResponseData<BorrowResponse>> addBorrow(@Valid @RequestBody BorrowRequest request, Errors errors){
        ResponseData<BorrowResponse> responseData = new ResponseData<>();
        try{
        if(errors.hasErrors()){
            for (ObjectError error : errors.getAllErrors()) {
                responseData.getMessages().add(error.getDefaultMessage());
            }
            responseData.setStatus(false);
            responseData.setPayload(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }
        BorrowResponse borrowResponse = borrowService.addBorrow(request);
        responseData.setStatus(true);
        responseData.setPayload(borrowResponse);
        responseData.getMessages().add("Borrow added successfully");
        return ResponseEntity.ok(responseData);
    }catch(Exception ex){
        responseData.setStatus(false);
        responseData.setPayload(null);
        responseData.getMessages().add(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
    }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseData<BorrowResponse>> updateBorrow(@PathVariable("id") long id, @Valid @RequestBody BorrowRequest request, Errors errors){
        ResponseData<BorrowResponse> responseData = new ResponseData<>();
        try{
            if(errors.hasErrors()){
                for (ObjectError error : errors.getAllErrors()) {
                    responseData.getMessages().add(error.getDefaultMessage());
                }
                responseData.setStatus(false);
                responseData.setPayload(null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
            }
            BorrowResponse borrowResponse = borrowService.updateBorrow(id, request);
            responseData.setStatus(true);
            responseData.setPayload(borrowResponse);
            responseData.getMessages().add("Borrow updated successfully");
            return ResponseEntity.ok(responseData);
        }catch(Exception ex){
            responseData.setStatus(false);
            responseData.setPayload(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }
}
