package com.elibrary.services;

import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.elibrary.dto.request.BookRequest;
import com.elibrary.dto.response.BookResponse;
import com.elibrary.dto.response.CategoryResponse;
import com.elibrary.model.entity.Book;
import com.elibrary.model.repos.BookRepo;
import com.elibrary.model.specification.BookSpecification;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepo bookRepo;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookSpecification bookSpecification;
    
    public BookResponse findByIdBookResponse(long id){
        Optional<Book> book = bookRepo.findById(id);
        if(book.isPresent()){
            return convertBookToBookResponse(book.get());
        }   
        return null;
    }

    public Book findById(long id){
        Optional<Book> book = bookRepo.findById(id);
        if(book.isPresent()){
            return book.get();
        }   
        return null;
    }
    
    public BookResponse convertBookToBookResponse(Book book) {
        BookResponse bookResponse = new BookResponse();
        bookResponse.setId(book.getId());
        bookResponse.setTitle(book.getTitle());
        bookResponse.setAuthor(book.getAuthor());
        bookResponse.setPublisher(book.getPublisher());
        bookResponse.setYearPublication(book.getYearPublication());
        bookResponse.setQuantity(book.getQuantity());
        bookResponse.setCategory(book.getCategory());
        bookResponse.setImage(book.getImage());
        return bookResponse;
    }

    public BookRequest convertBookResponseToBookRequest(BookResponse bookResponse){
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle(bookResponse.getTitle());
        bookRequest.setAuthor(bookResponse.getAuthor());
        bookRequest.setPublisher(bookResponse.getPublisher());
        bookRequest.setYearPublication(bookResponse.getYearPublication());
        bookRequest.setQuantity(bookResponse.getQuantity());
        bookRequest.setCategory(bookResponse.getCategory().getId());
        return bookRequest;
    }

    public BookResponse createBook(BookRequest request){
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setYearPublication(request.getYearPublication());
        book.setQuantity(request.getQuantity());
        CategoryResponse categoryResponse = categoryService.findById(request.getCategory());
        book.setCategory(categoryService.convertCategoryResponseToCategory(categoryResponse));
        bookRepo.save(book);
        return convertBookToBookResponse(book);
    }

    public BookResponse updateBook(long id, BookRequest request){
        Book book = findById(id);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setYearPublication(request.getYearPublication());
        book.setQuantity(request.getQuantity());
        CategoryResponse categoryResponse = categoryService.findById(request.getCategory());
        book.setCategory(categoryService.convertCategoryResponseToCategory(categoryResponse));
        bookRepo.save(book);
        return convertBookToBookResponse(book);
    }

    public void deleteBook(long id){
        bookRepo.deleteById(id);
    }

    public boolean existsByTitle(String title){
        return bookRepo.existsByTitle(title);
    }

    public Book findByTitle(String title){
        return bookRepo.findByTitle(title);
    }

    public BookResponse findByTitleResponse(String title){
        Book book = bookRepo.findByTitle(title);
        return convertBookToBookResponse(book);
    }

    public Page<BookResponse> searchBook(String search, Integer page, Integer size, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Book> listBook = bookRepo.findAll(bookSpecification.searchBook(search), pageable);
        int totalElements = (int) listBook.getTotalElements();
        return new PageImpl<>(listBook.stream().map(this::convertBookToBookResponse)
                                        .collect(Collectors.toList()), pageable, totalElements);
    }

}
