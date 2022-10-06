package com.elibrary.services;

import com.elibrary.dto.request.BookRequestdto;
import com.elibrary.dto.response.BookResponse;
import com.elibrary.dto.response.CategoryResponse;
import com.elibrary.model.entity.Book;
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
            book.getSynopsis(),
            book.getImage()
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

    public BookResponse createBook(BookRequestdto request){
        Book book = convertBookRequestdtoToBook(request);
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
        book.setSynopsis(request.getSynopsis());
        bookRepo.save(book);
        return convertBookToBookResponse(book);
    }

    public void deleteBook(long id){
        Book book = findById(id);
        book.setQuantity(0);
        bookRepo.save(book);
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
                                book.getSynopsis(),
                                book.getImage()))
                                        .collect(Collectors.toList()), pageable, totalElements);
    }

    public Page<BookResponse> getBooksByCategory(Long id, String search, Integer page, Integer size, String sortBy, String direction){
        Pageable pageable;
        if(direction.equals("desc")){
            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        } else{
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        }
        Page<Book> listBook = bookRepo.findBooksByCategory(id, search.toLowerCase(), pageable);
        int totalElements = (int) listBook.getTotalElements();
        return new PageImpl<>(listBook.stream().map(book ->
                        new BookResponse( book.getId(),
                                book.getTitle(),
                                book.getAuthor(),
                                book.getPublisher(),
                                book.getYearPublication(),
                                book.getQuantity() - borrowRepo.countBookBorrow(book.getId()),
                                book.getCategory(),
                                book.getSynopsis(),
                                book.getImage()))
                                        .collect(Collectors.toList()), pageable, totalElements);
    }

    public BookResponse findByIdBook(long id){
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
                    book.get().getSynopsis(),
                    book.get().getImage()
            );
        }
        return null;
    }

    public BookResponse uploadImage(MultipartFile image, long id) throws IOException {
        String originalNameImage = image.getOriginalFilename();
        int index = originalNameImage.lastIndexOf(".");

        String formatImage = "";
        if(index > 0){
            formatImage = "." + originalNameImage.substring(index + 1);
        }
        String imageName = UUID.randomUUID().toString() + formatImage;
        String userDirectory = Paths.get("").toAbsolutePath().toString();
        Book book = findById(id);

        image.transferTo(new File(userDirectory + "/src/main/resources/images/" + imageName));
        book.setImage(imageName);

        return convertBookToBookResponse(bookRepo.save(book));
    }

    public void save(Book book){
        bookRepo.save(book);
    }

    public void delete(long id){
        bookRepo.deleteById(id);
    }

}