package com.elibrary.controllers;

import com.elibrary.Constans;
import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.CategoryException;
import com.elibrary.dto.request.BookRequestdto;
import com.elibrary.dto.response.BookResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    
    private final BookService bookService;

    @PostMapping
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<BookResponse>> addBook(@Valid @RequestBody BookRequestdto bookRequestdto) throws CategoryException, BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
        if (bookService.existsByTitle(bookRequestdto.getTitle())) {
            BookResponse bookResponseFind = bookService.findByTitleResponse(bookRequestdto.getTitle());
            if (bookService.existsByTitle(bookRequestdto.getTitle()) &&
                    bookResponseFind.getAuthor().equals(bookRequestdto.getAuthor()) &&
                    bookResponseFind.getPublisher().equals(bookRequestdto.getPublisher()) &&
                    bookResponseFind.getYearPublication().equals(bookRequestdto.getYearPublication())) {

                bookRequestdto.setQuantity(bookResponseFind.getQuantity() + bookRequestdto.getQuantity());
                BookResponse bookResponse = bookService.updateBook(bookResponseFind.getId(), bookRequestdto);

                messagesList.put(Constans.MESSAGE, "Book with title " + bookRequestdto.getTitle() +
                        ", author" + bookRequestdto.getAuthor() +
                        ", publisher " + bookRequestdto.getPublisher() +
                        ", and year publication " + bookRequestdto.getYearPublication() +
                        " already exist. Quantity updated");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, bookResponse));
            }
        }
        BookResponse bookResponse = bookService.createBook(bookRequestdto);
        messagesList.put(Constans.MESSAGE,"Book successfully added");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookResponse));
    }

    @PutMapping("/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<BookResponse>> updateBook(@PathVariable("id") long id, @Valid @RequestBody BookRequestdto request) throws BusinessNotFound, CategoryException {
        Map<String, String> messagesList = new HashMap<>();
        BookResponse bookResponse = bookService.updateBook(id, request);
        messagesList.put(Constans.MESSAGE, "Book successfully updated");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookResponse));
    }

    @DeleteMapping("/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<?>> deleteBook(@PathVariable("id") long id) throws BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
        bookService.deleteBook(id);
        messagesList.put(Constans.MESSAGE, "Book quantity has been updated to 0");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @DeleteMapping("/delete/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<?>> deleteBookPermanently(@PathVariable("id") long id) throws BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
        bookService.delete(id);
        messagesList.put(Constans.MESSAGE, "Book has been deleted permanently");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @GetMapping("/public")
    public ResponseEntity<ResponseData<Page<BookResponse>>> searchBook(@RequestParam(defaultValue = "") String search,
                                                                       @RequestParam(defaultValue = "0") Long category,
                                                                       @RequestParam(defaultValue = "0") Integer page,
                                                                       @RequestParam(defaultValue = "10") Integer size,
                                                                       @RequestParam(defaultValue = "id") String sortBy,
                                                                       @RequestParam(defaultValue = "asc") String direction) throws BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
        Page<BookResponse> response;
        if (category == 0) {
            response = bookService.searchBook(search, page, size, sortBy, direction.toLowerCase());
        } else {
            response = bookService.getBooksByCategory(category, search, page, size, sortBy, direction.toLowerCase());
        }
        messagesList.put(Constans.MESSAGE, "Books retrieved successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, response));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ResponseData<BookResponse>> getBookById(@PathVariable("id") long id) throws BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
        BookResponse bookResponse = bookService.findByIdBook(id);
        messagesList.put(Constans.MESSAGE, "Book retrieved successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, bookResponse));
    }

}