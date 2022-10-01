package com.elibrary.controllers;

import com.elibrary.dto.request.BookRequestdto;
import com.elibrary.dto.response.BookResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.BookService;
import com.elibrary.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
@RequestMapping("/books")
public class BookController {
    
    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;


    @PostMapping("/employee/add")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<BookResponse>> addBook(@Valid @RequestBody BookRequestdto bookRequestdto, Errors errors){
        List<String> messagesList = new ArrayList<>();
        if(errors.hasErrors()){
            for (ObjectError error : errors.getAllErrors()) {
                messagesList.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
        }
        if(bookService.existsByTitle(bookRequestdto.getTitle())){
            BookResponse bookResponseFind = bookService.findByTitleResponse(bookRequestdto.getTitle());
            if(bookService.existsByTitle(bookRequestdto.getTitle()) &&
            bookResponseFind.getAuthor().equals(bookRequestdto.getAuthor()) &&
            bookResponseFind.getPublisher().equals(bookRequestdto.getPublisher()) &&
            bookResponseFind.getYearPublication().equals(bookRequestdto.getYearPublication())){

            bookRequestdto.setQuantity(bookResponseFind.getQuantity()+bookRequestdto.getQuantity());
            BookResponse bookResponse = bookService.updateBook(bookResponseFind.getId(), bookRequestdto);

            messagesList.add("Book with title " + bookRequestdto.getTitle() +
                                            ", author" + bookRequestdto.getAuthor() +
                                            ", publisher " + bookRequestdto.getPublisher() +
                                            ", and year publication " + bookRequestdto.getYearPublication() +
                                            " already exist. Quantity updated");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, bookResponse));
            }
        }
        BookResponse bookResponse = bookService.createBook(bookRequestdto);
            messagesList.add("Book successfully added");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookResponse));
    }

    @PutMapping("/employee/update/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<BookResponse>> updateBook(@PathVariable("id") long id, @Valid @RequestBody BookRequestdto request, Errors errors){
        List<String> messagesList = new ArrayList<>();
        if(errors.hasErrors()){
            for (ObjectError error : errors.getAllErrors()) {
                messagesList.add(error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
        }
        BookResponse bookResponse = bookService.updateBook(id, request);
        messagesList.add("Book has been updated");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookResponse));
    }

    @DeleteMapping("/employee/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<?>> deleteBook(@PathVariable("id") long id){
        List<String> messagesList = new ArrayList<>();
        if(!bookService.existsById(id)){
            messagesList.add("Book does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false,  messagesList, null));
        }
        bookService.deleteBook(id);
        messagesList.add("Book quantity has been updated to 0");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @DeleteMapping("/employee/delete/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<?>> deleteBookPermanently(@PathVariable("id") long id){
        List<String> messagesList = new ArrayList<>();
        if(!bookService.existsById(id)){
            messagesList.add("Book does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false,  messagesList, null));
        }
        bookService.delete(id);
        messagesList.add("Book has been deleted permanently");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @GetMapping("/public/allbooks")
    public ResponseEntity<ResponseData<Page<BookResponse>>> searchBook(@RequestParam(defaultValue = "") String search,
                                                                       @RequestParam(defaultValue = "0") Long category,
                                                                       @RequestParam(defaultValue = "0") Integer page,
                                                                       @RequestParam(defaultValue = "10") Integer size,
                                                                       @RequestParam(defaultValue = "id") String sortBy,
                                                                       @RequestParam(defaultValue = "asc") String direction){
        List<String> messagesList = new ArrayList<>();
        if(category == 0) {
            Page<BookResponse> response = bookService.searchBook(search, page, size, sortBy, direction.toLowerCase());
            messagesList.add("Books retrieved successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, response));
        }else{
            if (!categoryService.existsById(category)) {
                messagesList.add("Category does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false, messagesList, null));
            }
            Page<BookResponse> response = bookService.getBooksByCategory(category, search, page, size, sortBy, direction.toLowerCase());
            messagesList.add("Books retrieved successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, response));
        }
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ResponseData<BookResponse>> getBookById(@PathVariable("id") long id){
        List<String> messagesList = new ArrayList<>();
        if(!bookService.existsById(id)){
            messagesList.add("Book not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false, messagesList, null));
        }
        BookResponse bookResponse = bookService.findByIdBook(id);
        messagesList.add("Book retrieved successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookResponse));
    }

}
