package com.elibrary.controllers;

import com.elibrary.config.OneSignalConfig;
import com.elibrary.dto.request.BorrowRequestAdd;
import com.elibrary.dto.request.BorrowRequestUpdate;
import com.elibrary.dto.response.BorrowOvertimeResponse;
import com.elibrary.dto.response.BorrowResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.BookService;
import com.elibrary.services.BorrowService;
import com.elibrary.services.UserService;
import com.mashape.unirest.http.exceptions.UnirestException;
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
@RequestMapping("/borrows")
public class BorrowController {
    @Autowired
    private BorrowService borrowService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private OneSignalConfig oneSignalConfig;

    @PostMapping("/employee/add")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<List<BorrowResponse>>> addBorrow(@Valid @RequestBody List<BorrowRequestAdd> request, Errors errors) throws UnirestException {
        List<String> messagesList = new ArrayList<>();
        try{
            if(errors.hasErrors()){
                for (ObjectError error : errors.getAllErrors()) {
                    messagesList.add(error.getDefaultMessage());
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            if(request.size() > 3){
                messagesList.add("Borrow request is more than 3");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            if(borrowService.checkDuplicateValue(request)){
                messagesList.add("User can only borrow one book");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            List<String> message = borrowService.checkUserBorrows(request);
            if(message.size() > 0){
                String messageString = String.join(", ", message);
                messagesList.add(messageString);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            if(borrowService.countUserBorrows(request.get(0).getUserId()) >= 3){
                messagesList.add("User has reached maximum borrow limit");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            for(BorrowRequestAdd borrowRequestAdd : request){
                if(borrowService.countBookBorrow(borrowRequestAdd.getBookId()) - bookService.findById(borrowRequestAdd.getBookId()).getQuantity() == 0){
                    messagesList.add("Book " + bookService.findById(borrowRequestAdd.getBookId()).getTitle()  + " is not available");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
                }
            }
            if(borrowService.countUserBorrows(request.get(0).getUserId()) + request.size() > 3){
                messagesList.add("User can only borrow " + (3 - borrowService.countUserBorrows(request.get(0).getUserId())) + " books");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            List<BorrowResponse> borrowResponse = borrowService.addBorrows(request);
            StringBuilder book = new StringBuilder();
            for (BorrowResponse response : borrowResponse) {
                book.append(response.getBookTitle()).append(", ");
            }
            messagesList.add("Success borrow book " + book + "and due date is " + borrowResponse.get(0).getReturnDate());
            oneSignalConfig.pushNotifyBorrow(userService.findById(borrowResponse.get(0).getUserId()).getEmail(), borrowResponse.get(0).getFullName(),   " You have borrowed a book " + book + " and due date is " + borrowResponse.get(0).getReturnDate());
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponse));
        }catch (Exception e){
                messagesList.add(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @PutMapping("/employee/update/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<BorrowResponse>> updateBorrow(@PathVariable("id") long id, @Valid @RequestBody BorrowRequestUpdate request, Errors errors) throws UnirestException {
        List<String> messagesList = new ArrayList<>();
        try {
            if (!borrowService.existsById(id)) {
                messagesList.add("Borrow not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false, messagesList, null));
            }
            if (errors.hasErrors()) {
                for (ObjectError error : errors.getAllErrors()) {
                    messagesList.add(error.getDefaultMessage());
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            BorrowResponse borrowResponse = borrowService.updateBorrow(id, request);
            if (request.isReturned()) {
                long diff = borrowService.daydiff(borrowResponse.getBorrowDate());
                messagesList.add(borrowService.fineOrPenalty(diff, request.getPenalty()));
            } else {
                messagesList.add("Borrow updated successfully");
            }
            if (request.isReturned()) {
                oneSignalConfig.pushNotifyBorrow(userService.findById(borrowResponse.getUserId()).getEmail(), borrowResponse.getFullName(), "You have returned a book " + borrowResponse.getBookTitle() + ", and " + messagesList.get(0));
            }

            return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponse));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/employee")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<Page<BorrowResponse>>> getAllBorrows(@RequestParam(defaultValue = "") String search,
                                                                            @RequestParam(defaultValue = "false") boolean returned,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            @RequestParam(defaultValue = "id") String sortBy,
                                                                            @RequestParam(defaultValue = "asc") String direction){
        List<String> messagesList = new ArrayList<>();
        try {
            Page<BorrowResponse> borrowResponses = borrowService.findAllBorrows(search, returned, size, page, sortBy, direction.toLowerCase());
            messagesList.add("Borrows fetched successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponses));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/employee/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<BorrowResponse>> getBorrowById(@PathVariable("id") long id){
        List<String> messagesList = new ArrayList<>();
        try {
            if (!borrowService.existsById(id)) {
                messagesList.add("Borrow not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false, messagesList, null));
            }
            BorrowResponse borrowResponse = borrowService.findByIdResponse(id);
            messagesList.add("Borrow fetched successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponse));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/employee/borrowsovertime")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<Page<BorrowOvertimeResponse>>> getBorrowsOverTime(@RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "10") int size,
                                                                                         @RequestParam(defaultValue = "id") String sortBy,
                                                                                         @RequestParam(defaultValue = "asc") String direction){
        List<String> messagesList = new ArrayList<>();
        try {
            Page<BorrowOvertimeResponse> borrowResponses = borrowService.getBorrowOvertime(page, size, sortBy, direction.toLowerCase());
            messagesList.add("Borrows overtime fetched successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponses));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/member")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<Page<BorrowResponse>>> borrowsByMember(Principal principal,
                                                                                 @RequestParam(defaultValue = "false") boolean returnStatus,
                                                                                 @RequestParam(defaultValue = "0") Integer page,
                                                                                 @RequestParam(defaultValue = "10") Integer size,
                                                                                 @RequestParam(defaultValue = "id") String sortBy,
                                                                                 @RequestParam(defaultValue = "asc") String direction){
        List<String> messagesList = new ArrayList<>();
        try {
            Page<BorrowResponse> borrowResponses = borrowService.filterBorrowsById(principal, returnStatus, size, page, sortBy, direction.toLowerCase());
            messagesList.add("borrows retrieved successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponses));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/member/{id}")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<BorrowResponse>> getBorrowByMemberById(Principal principal,
                                                                          @PathVariable("id") long id){
        List<String> messagesList = new ArrayList<>();
        try {
            if (!borrowService.existsById(id)) {
                messagesList.add("Borrow not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false, messagesList, null));
            }
            BorrowResponse borrowResponse = borrowService.findByIdResponse(id);
            if (!Objects.equals(userService.getProfile(principal).getId(), borrowResponse.getUserId())) {
                messagesList.add("You are not authorized to view this borrow");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseData<>(false, messagesList, null));
            }
            messagesList.add("borrow retrieved successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponse));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }
}
