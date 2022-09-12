package com.elibrary.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.elibrary.dto.request.BookRequestRequest;
import com.elibrary.dto.response.BookRequestResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.BookRequestService;
import com.elibrary.services.UserService;

@RestController
@RequestMapping("/bookrequests")
public class BookRequestController {

    @Autowired
    private BookRequestService bookRequestService;

    @Autowired
    private UserService userService;
    
    @PostMapping("/add")
    public ResponseEntity<ResponseData<BookRequestResponse>> createBookRequest(@Valid @RequestBody BookRequestRequest request, Errors errors){
        ResponseData<BookRequestResponse> responseData = new ResponseData<>();
        try{
            if(errors.hasErrors()){
                for (ObjectError error : errors.getAllErrors()) {
                    responseData.getMessages().add(error.getDefaultMessage());
                }
                responseData.setStatus(false);
                responseData.setPayload(null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            long userId = userService.getUser().getId();
            BookRequestResponse bookRequestReponse = bookRequestService.createBookRequest(userId, request);
            responseData.setStatus(true);
            responseData.setPayload(bookRequestReponse);
            responseData.getMessages().add("Book Request Created Successfully");
            return ResponseEntity.ok(responseData);
        }catch(Exception e){
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
        
    }
    @PutMapping("/employee/update/{id}")
        public ResponseEntity<ResponseData<BookRequestResponse>> updateBookRequest(@PathVariable("id") long id,@Valid @RequestBody BookRequestRequest request, Errors errors){
            ResponseData<BookRequestResponse> responseData = new ResponseData<>();
            try{
                if(errors.hasErrors()){
                    for (ObjectError error : errors.getAllErrors()) {
                        responseData.getMessages().add(error.getDefaultMessage());
                    }
                    responseData.setStatus(false);
                    responseData.setPayload(null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                BookRequestResponse bookRequestReponse = bookRequestService.updateBookRequest(id, request);
                responseData.setStatus(true);
                responseData.setPayload(bookRequestReponse);
                responseData.getMessages().add("Book Request Updated Successfully");
                return ResponseEntity.ok(responseData);
            }catch(Exception e){
                responseData.setStatus(false);
                responseData.setPayload(null);
                responseData.getMessages().add(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
            }
    }

    @GetMapping("/employee/allBookRequests")
    public ResponseEntity<ResponseData<Page<BookRequestResponse>>> getAllBookRequests(@RequestParam(defaultValue = "") String search,
                                                                                        @RequestParam(defaultValue = "0") Integer page, 
                                                                                        @RequestParam(defaultValue = "10") Integer size,
                                                                                        @RequestParam(defaultValue = "id") String sortBy){
        ResponseData<Page<BookRequestResponse>> responseData = new ResponseData<>();
        try{
            Page<BookRequestResponse> bookRequests = bookRequestService.searchBookRequest(search, page, size, sortBy);
            responseData.setStatus(true);
            responseData.setPayload(bookRequests);
            responseData.getMessages().add("Book Requests Retrieved Successfully");
            return ResponseEntity.ok(responseData);
        }catch(Exception e){
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }
    @GetMapping("/member/AllBookRequests")
    public ResponseEntity<ResponseData<Page<BookRequestResponse>>> getAllBookRequestsByMember(@RequestParam(defaultValue = "") String search,
                                                                                        @RequestParam(defaultValue = "0") Integer page, 
                                                                                        @RequestParam(defaultValue = "10") Integer size,
                                                                                        @RequestParam(defaultValue = "id") String sortBy){
        ResponseData<Page<BookRequestResponse>> responseData = new ResponseData<>();
        try{
            long userId = userService.getUser().getId();
            Page<BookRequestResponse> bookRequests = bookRequestService.filterById(search, userId, page, size, sortBy);
            responseData.setStatus(true);
            responseData.setPayload(bookRequests);
            responseData.getMessages().add("Book Requests Retrieved Successfully");
            return ResponseEntity.ok(responseData);
        }catch(Exception e){
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<ResponseData<BookRequestResponse>> findById(@PathVariable("id")long id){
        ResponseData<BookRequestResponse> responseData = new ResponseData<>();
        try{
            BookRequestResponse bookRequestResponse = bookRequestService.filterById(id);
            responseData.setStatus(true);
            responseData.setPayload(bookRequestResponse);
            responseData.getMessages().add("book request retrieved successfully");
            return ResponseEntity.ok(responseData);
        }catch(Exception e){
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    @DeleteMapping("/employee/{id}")
    public ResponseEntity<ResponseData<String>> deleteBookRequest(@PathVariable("id") long id){
        ResponseData<String> responseData = new ResponseData<>();
        try{
            bookRequestService.delete(id);
            responseData.setStatus(true);
            responseData.setPayload(null);
            responseData.getMessages().add("Book Request Deleted Successfully");
            return ResponseEntity.ok(responseData);
        }catch(Exception e){
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }
}
