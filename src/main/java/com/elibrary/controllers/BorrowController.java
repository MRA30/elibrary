package com.elibrary.controllers;

import com.elibrary.Constans;
import com.elibrary.Exception.BorrowException;
import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.ForbiddenException;
import com.elibrary.config.OneSignalConfig;
import com.elibrary.dto.request.BorrowRequestAdd;
import com.elibrary.dto.request.BorrowRequestUpdate;
import com.elibrary.dto.response.BorrowOvertimeResponse;
import com.elibrary.dto.response.BorrowResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.BorrowService;
import com.elibrary.services.UserService;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    private final UserService userService;

    private final OneSignalConfig oneSignalConfig;

    @PostMapping
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<List<BorrowResponse>>> addBorrow(@Valid @RequestBody List<BorrowRequestAdd> request) throws UnirestException, BorrowException {
        Map<String, String> messagesList = new HashMap<>();
        List<BorrowResponse> borrowResponse = borrowService.addBorrows(request);
        StringBuilder book = new StringBuilder();
        for (BorrowResponse response : borrowResponse) {
            book.append(response.getBookTitle()).append(", ");
        }
        messagesList.put(Constans.MESSAGE, "Success borrow book " + book + "and due date is " + borrowResponse.get(0).getReturnDate());
        oneSignalConfig.pushNotifyBorrow(userService.findById(borrowResponse.get(0).getUserId()).getEmail(), borrowResponse.get(0).getFullName(),   " You have borrowed a book " + book + " and due date is " + borrowResponse.get(0).getReturnDate());
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponse));
    }

    @PutMapping("/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<BorrowResponse>> updateBorrow(@PathVariable("id") long id, @Valid @RequestBody BorrowRequestUpdate request)
        throws UnirestException, BusinessNotFound, ParseException {
        Map<String, String> messagesList = new HashMap<>();
        BorrowResponse borrowResponse = borrowService.updateBorrow(id, request);
        if (request.isReturned()) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date borrowDate = formatter.parse(borrowResponse.getBorrowDate());
            long diff = borrowService.daydiff(borrowDate);
            messagesList.put(Constans.MESSAGE, borrowService.fineOrPenalty(diff, request.getPenalty()));
            oneSignalConfig.pushNotifyBorrow(userService.findById(borrowResponse.getUserId()).getEmail(), borrowResponse.getFullName()," You have returned a book " + borrowResponse.getBookTitle() + " and " + messagesList.get(Constans.MESSAGE));
        } else {
            messagesList.put(Constans.MESSAGE, "Borrow updated successfully");
        }
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponse));
    }

    @GetMapping
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<Page<BorrowResponse>>> getAllBorrows(@RequestParam(defaultValue = "") String search,
                                                                            @RequestParam(defaultValue = "false") boolean returned,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            @RequestParam(defaultValue = "id") String sortBy,
                                                                            @RequestParam(defaultValue = "asc") String direction){
        Map<String, String> messagesList = new HashMap<>();
        try {
            Page<BorrowResponse> borrowResponses = borrowService.findAllBorrows(search, returned, size, page, sortBy, direction.toLowerCase());
            messagesList.put(Constans.MESSAGE, "Borrows fetched successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponses));
        }catch (Exception e){
            messagesList.put(Constans.MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<BorrowResponse>> getBorrowById(@PathVariable("id") long id) throws BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
        BorrowResponse borrowResponse = borrowService.findByIdResponse(id);
        messagesList.put(Constans.MESSAGE, "Borrow fetched successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponse));
    }

    @GetMapping("/borrows-overtime")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<Page<BorrowOvertimeResponse>>> getBorrowsOverTime(@RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "10") int size,
                                                                                         @RequestParam(defaultValue = "id") String sortBy,
                                                                                         @RequestParam(defaultValue = "asc") String direction){
        Map<String, String> messagesList = new HashMap<>();
        try {
            Page<BorrowOvertimeResponse> borrowResponses = borrowService.getBorrowOvertime(page, size, sortBy, direction.toLowerCase());
            messagesList.put(Constans.MESSAGE, "Borrows overtime fetched successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponses));
        }catch (Exception e){
            messagesList.put(Constans.MESSAGE, e.getMessage());
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
        Map<String, String> messagesList = new HashMap<>();
        try {
            Page<BorrowResponse> borrowResponses = borrowService.filterBorrowsById(principal, returnStatus, size, page, sortBy, direction.toLowerCase());
            messagesList.put(Constans.MESSAGE, "borrows retrieved successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponses));
        }catch (Exception e){
            messagesList.put(Constans.MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/member/{id}")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<BorrowResponse>> getBorrowByMemberById(Principal principal,
                                                                          @PathVariable("id") long id) throws BusinessNotFound, ForbiddenException {
        Map<String, String> messagesList = new HashMap<>();
        long userId = userService.getProfile(principal).getId();
        BorrowResponse borrowResponse = borrowService.findByIdResponseMember(id, userId);
        messagesList.put(Constans.MESSAGE, "borrow retrieved successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, borrowResponse));
    }
}
