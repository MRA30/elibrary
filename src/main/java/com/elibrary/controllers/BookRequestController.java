package com.elibrary.controllers;

import com.elibrary.Constans;
import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.ForbiddenException;
import com.elibrary.dto.request.BookRequestRequest;
import com.elibrary.dto.response.BookRequestResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.BookRequestService;
import com.elibrary.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/bookrequests")
public class BookRequestController {

    @Autowired
    private BookRequestService bookRequestService;

    @Autowired
    private UserService userService;
    
    @PostMapping("/add")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<BookRequestResponse>> createBookRequest(@Valid @RequestBody BookRequestRequest request, Principal principal){
        Map<String, String> messagesList = new HashMap<>();
        long userId = userService.getProfile(principal).getId();
        BookRequestResponse bookRequestResponse = bookRequestService.createBookRequest(userId, request);
        messagesList.put(Constans.MESSAGE, "Book Request Created Successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequestResponse));
    }
    @PutMapping("/employee/update/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<BookRequestResponse>> updateBookRequest(@PathVariable("id") long id,@Valid @RequestBody BookRequestRequest request) throws BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
        BookRequestResponse bookRequestResponse = bookRequestService.updateBookRequest(id, request);
        messagesList.put(Constans.MESSAGE, "Book Request Updated Successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequestResponse));
    }

     @GetMapping("/employee")
     @RolesAllowed("employee")
     public ResponseEntity<ResponseData<Page<BookRequestResponse>>> getAllBookRequests(@RequestParam(defaultValue = "") String search,
                                                                                       @RequestParam(defaultValue = "false") boolean available,
                                                                                       @RequestParam(defaultValue = "0") Integer page,
                                                                                       @RequestParam(defaultValue = "10") Integer size,
                                                                                       @RequestParam(defaultValue = "id") String sortBy,
                                                                                       @RequestParam(defaultValue = "asc") String direction){
         Map<String, String> messagesList = new HashMap<>();
        try {
            Page<BookRequestResponse> bookRequests = bookRequestService.searchBookRequest(search, available, page, size, sortBy, direction.toLowerCase());
            messagesList.put(Constans.MESSAGE, "Book Requests Retrieved Successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequests));
        }catch (Exception e){
            messagesList.put(Constans.MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
     }

    @DeleteMapping("/employee/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<String>> deleteBookRequest(@PathVariable("id") long id) throws BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
        bookRequestService.delete(id);
        messagesList.put(Constans.MESSAGE, "Book Request Deleted Successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }


     @GetMapping("/employee/{id}")
     @RolesAllowed("employee")
     public ResponseEntity<ResponseData<BookRequestResponse>> findById(@PathVariable("id")long id) throws BusinessNotFound {
         Map<String, String> messagesList = new HashMap<>();
         BookRequestResponse bookRequestResponse = bookRequestService.findByIdResponse(id);
         messagesList.put(Constans.MESSAGE, "book request retrieved successfully");
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
        Map<String, String> messagesList = new HashMap<>();
        try {
            long userId = userService.getProfile(principal).getId();
            Page<BookRequestResponse> bookRequests = bookRequestService.filterByUserId(search, userId, available, page, size, sortBy, direction.toLowerCase());
            messagesList.put(Constans.MESSAGE, "Book Requests Retrieved Successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequests));
        }catch (Exception e){
            messagesList.put(Constans.MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

     @GetMapping("/member/{id}")
     @RolesAllowed("member")
     public ResponseEntity<ResponseData<BookRequestResponse>> findByIdByMember(@PathVariable("id")long id, Principal principal) throws BusinessNotFound, ForbiddenException {
        Map<String, String> messagesList = new HashMap<>();
        long userId = userService.getProfile(principal).getId();
        BookRequestResponse bookRequestResponse = bookRequestService.findByIdResponseMember(id, userId);
        messagesList.put(Constans.MESSAGE, "book request retrieved successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookRequestResponse));
    }
}
