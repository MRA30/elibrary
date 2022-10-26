package com.elibrary.services;

import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.CategoryException;
import com.elibrary.dto.request.BookRequestdto;
import com.elibrary.dto.response.BookResponse;
import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.Category;
import com.elibrary.model.repos.BookRepo;
import com.elibrary.model.repos.BorrowRepo;
import com.elibrary.model.repos.ImageRepo;
import com.elibrary.model.specification.BookSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

    private final BookRepo bookRepo;

    private final BorrowRepo borrowRepo;

    private final CategoryService categoryService;

    private final BookSpecification bookSpecification;

    private final ImageRepo imageRepo;

    public List<String> findAllImage(String type, long id){
        return imageRepo.findAllImageByTypeAndId(type, id);
    }

    public Book save(Book book){
        if(book.getId() != null){
            Book currentBook = findById(book.getId());
            currentBook.setTitle(book.getTitle());
            currentBook.setAuthor(book.getAuthor());
            currentBook.setPublisher(book.getPublisher());
            currentBook.setYearPublication(book.getYearPublication());
            currentBook.setQuantity(book.getQuantity());
            currentBook.setCategory(book.getCategory());
            currentBook.setSynopsis(book.getSynopsis());
            book = currentBook;
        }
        return bookRepo.save(book);
    }

    public void delete(long id) throws BusinessNotFound {
        Optional<Book> book = bookRepo.findById(id);
        if(book.isPresent()){
            bookRepo.deleteById(id);
        }else{
            throw new BusinessNotFound("Book not found");
        }
    }

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
            book.getCategory().getId(),
            book.getCategory().getCategory(),
            book.getSynopsis(),
            findAllImage("book", book.getId())
        );
    }

    public Book convertBookRequestdtoToBook(BookRequestdto bookRequestdto) {
        return new Book(
            bookRequestdto.getTitle(),
            bookRequestdto.getAuthor(),
            bookRequestdto.getPublisher(),
            bookRequestdto.getYearPublication(),
            bookRequestdto.getQuantity(),
            categoryService.findById(bookRequestdto.getCategory()),
            bookRequestdto.getSynopsis()
        );
    }

    public boolean existsById(long id){
        return bookRepo.existsById(id);
    }

    public BookResponse createBook(BookRequestdto request) throws CategoryException {
        if(categoryService.existsById(request.getCategory())){
            Book book = convertBookRequestdtoToBook(request);
            return convertBookToBookResponse(save(book));
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
                Category category = categoryService.findById(request.getCategory());
                book.setCategory(category);
                book.setSynopsis(request.getSynopsis());
                return convertBookToBookResponse(save(book));
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
                                book.getCategory().getId(),
                                book.getCategory().getCategory(),
                                book.getSynopsis(),
                                findAllImage("book", book.getId())))
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
                                    book.getCategory().getId(),
                                    book.getCategory().getCategory(),
                                    book.getSynopsis(),
                                    findAllImage("book", book.getId())))
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
                    book.get().getCategory().getId(),
                    book.get().getCategory().getCategory(),
                    book.get().getSynopsis(),
                    findAllImage("book", book.get().getId())
            );
        }
        throw new BusinessNotFound("Book not found");
    }

}