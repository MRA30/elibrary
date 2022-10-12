package com.elibrary.services;

import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.CategoryException;
import com.elibrary.dto.request.BookRequestdto;
import com.elibrary.dto.response.BookResponse;
import com.elibrary.dto.response.CategoryResponse;
import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.Image;
import com.elibrary.model.repos.BookRepo;
import com.elibrary.model.repos.BorrowRepo;
import com.elibrary.model.specification.BookSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepo bookRepo;

    @Autowired
    private BorrowRepo borrowRepo;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookSpecification bookSpecification;


    public Book findById(long id){
        Optional<Book> book = bookRepo.findById(id);
        return book.orElse(null);
    }
    
    public BookResponse
    convertBookToBookResponse(Book book) {
        return new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPublisher(),
            book.getYearPublication(),
            book.getQuantity(),
            book.getCategory(),
            book.getSynopsis()
        );
    }

    public Book convertBookRequestdtoToBook(BookRequestdto bookRequestdto) {
        return new Book(
            bookRequestdto.getTitle(),
            bookRequestdto.getAuthor(),
            bookRequestdto.getPublisher(),
            bookRequestdto.getYearPublication(),
            bookRequestdto.getQuantity(),
            categoryService.convertCategoryResponseToCategory(categoryService.findById(bookRequestdto.getCategory())),
            bookRequestdto.getSynopsis()
        );
    }

    public boolean existsById(long id){
        return bookRepo.existsById(id);
    }

    public BookResponse createBook(BookRequestdto request) throws CategoryException {
        if(categoryService.existsById(request.getCategory())){
            Book book = convertBookRequestdtoToBook(request);
            bookRepo.save(book);
            return convertBookToBookResponse(book);
        }else {
            throw new CategoryException("Category does not exist");
        }

    }

    public BookResponse updateBook(long id, BookRequestdto request) throws BusinessNotFound, CategoryException {
        Book book = findById(id);
        if(book != null){
            if(categoryService.existsById(request.getCategory())) {

                book.setTitle(request.getTitle());
                book.setAuthor(request.getAuthor());
                book.setPublisher(request.getPublisher());
                book.setYearPublication(request.getYearPublication());
                book.setQuantity(request.getQuantity());
                CategoryResponse categoryResponse = categoryService.findById(request.getCategory());
                book.setCategory(categoryService.convertCategoryResponseToCategory(categoryResponse));
                book.setSynopsis(request.getSynopsis());
                bookRepo.save(book);
                return convertBookToBookResponse(book);
            }else {
                throw new CategoryException("Category does not exist");
            }
        }else {
            throw new BusinessNotFound("Book not found");
        }

    }

    public void deleteBook(long id) throws BusinessNotFound {
        Book book = findById(id);
        if(book != null){
            book.setQuantity(0);
            bookRepo.save(book);
        }else {
            throw new BusinessNotFound("Book not found");
        }
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

    public Page<BookResponse> searchBook(String search, Integer page, Integer size, String sortBy, String direction){
        Pageable pageable;
        if(direction.equals("desc")){
            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        } else{
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        }
        Page<Book> listBook = bookRepo.findAll(bookSpecification.searchBook(search), pageable);
        int totalElements = (int) listBook.getTotalElements();
        return new PageImpl<>(listBook.stream().map(book ->
                        new BookResponse( book.getId(),
                                book.getTitle(),
                                book.getAuthor(),
                                book.getPublisher(),
                                book.getYearPublication(),
                                book.getQuantity() - borrowRepo.countBookBorrow(book.getId()),
                                book.getCategory(),
                                book.getSynopsis()))
                                        .collect(Collectors.toList()), pageable, totalElements);
    }

    public Page<BookResponse> getBooksByCategory(Long id, String search, Integer page, Integer size, String sortBy, String direction) throws BusinessNotFound {
        if(categoryService.existsById(id)) {
            Pageable pageable;
            if (direction.equals("desc")) {
                pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
            } else {
                pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
            }
            Page<Book> listBook = bookRepo.findBooksByCategory(id, search.toLowerCase(), pageable);
            int totalElements = (int) listBook.getTotalElements();
            return new PageImpl<>(listBook.stream().map(book ->
                            new BookResponse(book.getId(),
                                    book.getTitle(),
                                    book.getAuthor(),
                                    book.getPublisher(),
                                    book.getYearPublication(),
                                    book.getQuantity() - borrowRepo.countBookBorrow(book.getId()),
                                    book.getCategory(),
                                    book.getSynopsis()))
                    .collect(Collectors.toList()), pageable, totalElements);
        }else {
            throw new BusinessNotFound("Category does not exist");
        }
    }

    public BookResponse findByIdBook(long id) throws BusinessNotFound {
        Optional<Book> book = bookRepo.findById(id);
        if (book.isPresent()){
            return new BookResponse(
                    book.get().getId(),
                    book.get().getTitle(),
                    book.get().getAuthor(),
                    book.get().getPublisher(),
                    book.get().getYearPublication(),
                    book.get().getQuantity() - borrowRepo.countBookBorrow(book.get().getId()),
                    book.get().getCategory(),
                    book.get().getSynopsis()
            );
        }
        throw new BusinessNotFound("Book not found");
    }

    public void save(Book book){
        bookRepo.save(book);
    }

    public void delete(long id) throws BusinessNotFound {
        Optional<Book> book = bookRepo.findById(id);
        if(book.isPresent()){
            bookRepo.deleteById(id);
        }else{
            throw new BusinessNotFound("Book not found");
        }
    }

}