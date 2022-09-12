package com.elibrary.services;

import java.util.List;
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

import com.elibrary.dto.request.BookRequestdto;
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
        return new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPublisher(),
            book.getYearPublication(),
            book.getQuantity(),
            book.getCategory(),
            book.getImage()
        );
    }

    public BookRequestdto convertBookResponseToBookRequest(BookResponse bookResponse){
        BookRequestdto bookRequest = new BookRequestdto();
        bookRequest.setTitle(bookResponse.getTitle());
        bookRequest.setAuthor(bookResponse.getAuthor());
        bookRequest.setPublisher(bookResponse.getPublisher());
        bookRequest.setYearPublication(bookResponse.getYearPublication());
        bookRequest.setQuantity(bookResponse.getQuantity());
        bookRequest.setCategory(bookResponse.getCategory().getId());
        return bookRequest;
    }

    public BookResponse createBook(BookRequestdto request){
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

    public BookResponse updateBook(long id, BookRequestdto request){
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

    public void save(Book book) {
        bookRepo.save(book);
    }

    public List<BookResponse> findAll() {
        List<Book> book = bookRepo.findAll();
        return book.stream().map(this::convertBookToBookResponse).collect(Collectors.toList());
    }

}