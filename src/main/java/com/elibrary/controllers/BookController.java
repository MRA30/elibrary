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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.elibrary.dto.request.BookRequestdto;
import com.elibrary.dto.response.BookResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.BookService;

@RestController
@RequestMapping("/books")
public class BookController {
    
    @Autowired
    private BookService bookService;


    @PostMapping("/add")
    public ResponseEntity<ResponseData<BookResponse>> addBook(@Valid @RequestBody BookRequestdto bookRequestdto, Errors errors){
        ResponseData<BookResponse> responseData = new ResponseData<>();
        try{
            if(errors.hasErrors()){
                for (ObjectError error : errors.getAllErrors()) {
                    responseData.getMessages().add(error.getDefaultMessage());
                }
                responseData.setStatus(false);
                responseData.setPayload(null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
            }
            if(bookService.existsByTitle(bookRequestdto.getTitle())){
                BookResponse bookResponseFind = bookService.findByTitleResponse(bookRequestdto.getTitle());
                if(bookService.existsByTitle(bookRequestdto.getTitle()) &&
                bookResponseFind.getAuthor().equals(bookRequestdto.getAuthor()) &&
                bookResponseFind.getPublisher().equals(bookRequestdto.getPublisher()) &&
                bookResponseFind.getYearPublication().equals(bookRequestdto.getYearPublication())){
                bookRequestdto.setQuantity(bookResponseFind.getQuantity()+bookRequestdto.getQuantity());
                BookResponse bookResponse = bookService.updateBook(bookResponseFind.getId(), bookRequestdto);
                responseData.setStatus(false);
                responseData.setPayload(bookResponse);
                responseData.getMessages().add("Book with title" + bookRequestdto.getTitle() + 
                                                ", author" + bookRequestdto.getAuthor() + 
                                                ", publisher " + bookRequestdto.getPublisher() + 
                                                ", and year publication " + bookRequestdto.getYearPublication() + 
                                                " already exist. Quantity updated");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
                }
            }
            BookResponse bookResponse = bookService.createBook(bookRequestdto);
                responseData.setStatus(true);
                responseData.setPayload(bookResponse);
                responseData.getMessages().add("Book added");
                return ResponseEntity.ok(responseData);
            
        }catch(Exception ex){
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add(ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ResponseData<BookResponse>> updateBook(@PathVariable("id") long id, @Valid @RequestBody BookRequestdto request, Errors errors){
        ResponseData<BookResponse> responseData = new ResponseData<>();
        try{
            if(errors.hasErrors()){
                for (ObjectError error : errors.getAllErrors()) {
                    responseData.getMessages().add(error.getDefaultMessage());
                }
                responseData.setStatus(false);
                responseData.setPayload(null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
            }
            if(bookService.existsByTitle(request.getTitle()) 
                && bookService.findByTitle(request.getTitle()).getId() != id){
                    responseData.setStatus(false);
                    responseData.setPayload(null);
                    responseData.getMessages().add("Book with title " + request.getTitle() + " already exists");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
                }
            BookResponse bookResponse = bookService.updateBook(id, request);
            responseData.setStatus(true);
            responseData.setPayload(bookResponse);
            responseData.getMessages().add("Book has been updated");
            return ResponseEntity.ok(responseData);
        }catch(Exception e){
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ResponseData<?>> deleteBook(@PathVariable("id") long id){
        ResponseData<?> responseData = new ResponseData<>();
        try{
            bookService.deleteBook(id);
            responseData.setStatus(true);
            responseData.setPayload(null);
            responseData.getMessages().add("Book has been deleted");
            return ResponseEntity.ok(responseData);
        }catch(Exception e){
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    @GetMapping
    public ResponseEntity<ResponseData<Page<BookResponse>>> searchBook(@RequestParam(defaultValue = "") String search,
                                                                        @RequestParam(defaultValue = "0") Integer page, 
                                                                        @RequestParam(defaultValue = "10") Integer size,
                                                                        @RequestParam(defaultValue = "id") String sortBy){
        ResponseData<Page<BookResponse>> responseData = new ResponseData<>();
        try{
            Page<BookResponse> response = bookService.searchBook(search, page, size, sortBy);
            responseData.setStatus(true);
            responseData.setPayload(response);
            responseData.getMessages().add("Data Loaded");
            return ResponseEntity.ok(responseData);
        }catch (Exception e){
            responseData.getMessages().add(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }
    }

}
