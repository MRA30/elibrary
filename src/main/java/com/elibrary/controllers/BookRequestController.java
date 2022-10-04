package com.elibrary.controllers;

import com.elibrary.dto.request.BookRequestRequest;
import com.elibrary.dto.response.BookRequestResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.BookRequestService;
import com.elibrary.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/bookrequests")
public class BookRequestController {

    @Autowired
    private BookRequestService bookRequestService;

    @Autowired
    private UserService userService;
    
    @PostMapping("/employee/add")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<BookRequestResponse>> createBookRequest(@Valid @RequestBody BookRequestRequest request, Principal principal, Errors errors){
        List<String> messagesList = new ArrayList<>();
//        if(errors.hasErrors()){
//            for (ObjectError error : errors.getAllErrors()) {
//                messagesList.add(error.getDefaultMessage());
//            }
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
//        }
        long userId = userService.getProfile(principal).getId();
        BookRequestResponse bookRequestResponse = bookRequestService.createBookRequest(userId, request);
        messagesList.add("Book Request Created Successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequestResponse));
    }
    @PutMapping("/employee/update/{id}")
    @RolesAllowed("employee")
        public ResponseEntity<ResponseData<BookRequestResponse>> updateBookRequest(@PathVariable("id") long id,@Valid @RequestBody BookRequestRequest request, Errors errors){
            List<String> messagesList = new ArrayList<>();
            if(!bookRequestService.existsById(id)){
                messagesList.add("Book Request does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false, messagesList, null));
            }
            if(errors.hasErrors()){
                for (ObjectError error : errors.getAllErrors()) {
                    messagesList.add(error.getDefaultMessage());
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            BookRequestResponse bookRequestReponse = bookRequestService.updateBookRequest(id, request);
            messagesList.add("Book Request Updated Successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequestReponse));
    }

     @GetMapping("/employee")
     @RolesAllowed("employee")
     public ResponseEntity<ResponseData<Page<BookRequestResponse>>> getAllBookRequests(@RequestParam(defaultValue = "") String search,
                                                                                       @RequestParam(defaultValue = "false") boolean available,
                                                                                       @RequestParam(defaultValue = "0") Integer page,
                                                                                       @RequestParam(defaultValue = "10") Integer size,
                                                                                       @RequestParam(defaultValue = "id") String sortBy,
                                                                                       @RequestParam(defaultValue = "asc") String direction){
        List<String> messagesList = new ArrayList<>();
        Page<BookRequestResponse> bookRequests = bookRequestService.searchBookRequest(search, available, page, size, sortBy, direction.toLowerCase());
         messagesList.add("Book Requests Retrieved Successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequests));
     }

    @DeleteMapping("/employee/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<String>> deleteBookRequest(@PathVariable("id") long id){
        List<String> messagesList = new ArrayList<>();
        if(!bookRequestService.existsById(id)){
            messagesList.add("Book Request does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false, messagesList, null));
        }
        bookRequestService.delete(id);
        messagesList.add("Book Request Deleted Successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }


     @GetMapping("/employee/{id}")
     @RolesAllowed("employee")
     public ResponseEntity<ResponseData<BookRequestResponse>> findById(@PathVariable("id")long id){
         List<String> messagesList = new ArrayList<>();
         if(!bookRequestService.existsById(id)){
             messagesList.add("Book Request Not Found");
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false, messagesList, null));
         }
         BookRequestResponse bookRequestResponse = bookRequestService.findByIdResponse(id);
         messagesList.add("book request retrieved successfully");
         return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequestResponse));
     }

    @GetMapping("/member")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<Page<BookRequestResponse>>> getAllBookRequestsByMember(Principal principal,
                                                                                              @RequestParam(defaultValue = "") String search,
                                                                                              @RequestParam(defaultValue = "false") boolean available,
                                                                                              @RequestParam(defaultValue = "0") Integer page,
                                                                                              @RequestParam(defaultValue = "10") Integer size,
                                                                                              @RequestParam(defaultValue = "id") String sortBy,
                                                                                              @RequestParam(defaultValue = "asc") String direction){
        List<String> messagesList = new ArrayList<>();
        long userId = userService.getProfile(principal).getId();
        Page<BookRequestResponse> bookRequests = bookRequestService.filterByUserId(search, userId, available, page, size, sortBy, direction.toLowerCase());
        messagesList.add("Book Requests Retrieved Successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequests));
    }

     @GetMapping("/member/{id}")
     @RolesAllowed("member")
        public ResponseEntity<ResponseData<BookRequestResponse>> findByIdByMember(@PathVariable("id")long id, Principal principal){
            List<String> messagesList = new ArrayList<>();
            if (!bookRequestService.existsById(id)) {
                messagesList.add("Book Request Not Found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false, messagesList, null));
            }
            BookRequestResponse bookRequestResponse = bookRequestService.findByIdResponse(id);
            if(!Objects.equals(bookRequestResponse.getUserId(), userService.getProfile(principal).getId())){
                messagesList.add("You are not authorized to view this book request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseData<>(false, messagesList, null));
            }
            messagesList.add("book request retrieved successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequestResponse));
        }
}
