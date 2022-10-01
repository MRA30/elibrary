package com.elibrary.services;

import com.elibrary.dto.request.BookRequestRequest;
import com.elibrary.dto.response.BookRequestResponse;
import com.elibrary.model.entity.BookRequest;
import com.elibrary.model.entity.User;
import com.elibrary.model.repos.BookRequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookRequestService {
    
    @Autowired
    private BookRequestRepo bookRequestRepo;

    @Autowired
    private UserService userService;

    public BookRequestResponse convertBookRequestToBookRequestReponse(BookRequest bookRequest){
        return new BookRequestResponse(
        bookRequest.getId(),
        bookRequest.getUser().getId(),
        bookRequest.getUser().getFirstName() +  " " + bookRequest.getUser().getLastName(),
        bookRequest.getTitle(),
        bookRequest.isAvailable(),
        bookRequest.getDescription()
        );

    }

    public boolean existsById(long id){
        return bookRequestRepo.existsById(id);
    }

    public BookRequest findById(long id){
        Optional<BookRequest> bookRequest = bookRequestRepo.findById(id);
        return bookRequest.orElse(null);
    }

    public BookRequestResponse findByIdResponse(long id){
        Optional<BookRequest> bookRequest = bookRequestRepo.findById(id);
        return bookRequest.map(this::convertBookRequestToBookRequestReponse).orElse(null);
    }

    public BookRequestResponse createBookRequest(long id, BookRequestRequest request){
        BookRequest bookRequest = new BookRequest();
        User user = userService.findById(id);
        bookRequest.setTitle(request.getTitle());
        bookRequest.setUser(user);
        bookRequest.setAvailable(false);
        bookRequest.setDescription(request.getDescription());
        return convertBookRequestToBookRequestReponse(bookRequestRepo.save(bookRequest));
    }

    public BookRequestResponse updateBookRequest(long id,BookRequestRequest request){
        BookRequest bookRequest = findById(id);
        bookRequest.setAvailable(request.isAvailable());
        bookRequest.setDescription(request.getDescription());
        return convertBookRequestToBookRequestReponse(bookRequestRepo.save(bookRequest));
    }

     public Page<BookRequestResponse> searchBookRequest(String search, boolean available, Integer page, Integer size, String sortBy, String direction){
         Pageable pageable;
         if(direction.equals("desc")){
             pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
         } else{
             pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
         }
         Page<BookRequest> listBookRequest = bookRequestRepo.findByTitleContainingIgnoreCase
                 (search.toLowerCase(), available, pageable);
         int totalElements = (int) listBookRequest.getTotalElements();
         return new PageImpl<>(listBookRequest.getContent()
                 .stream().map(this::convertBookRequestToBookRequestReponse).collect(Collectors.toList()),
                 pageable, totalElements);
     }

     public Page <BookRequestResponse> filterByUserId(String search, long userId,boolean available, Integer page, Integer size, String sortBy, String direction){
         Pageable pageable;
         if(direction.equals("desc")){
             pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
         } else{
             pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
         }
         Page<BookRequest> listBookRequest = bookRequestRepo.findByTitleContainingIgnoreCaseMember
                 (search.toLowerCase(), userId, available, pageable);
         int totalElements = (int) listBookRequest.getTotalElements();
         return new PageImpl<>(listBookRequest.getContent()
                 .stream().map(this::convertBookRequestToBookRequestReponse).collect(Collectors.toList()),
                 pageable, totalElements);
     }

    public void delete(long id){
        bookRequestRepo.deleteById(id);
    }

}
