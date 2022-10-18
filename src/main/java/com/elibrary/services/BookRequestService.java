package com.elibrary.services;

import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.ForbiddenException;
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

    public BookRequest save(BookRequest bookRequest) {
        if(bookRequest.getId() != null){
            BookRequest currentBookRequest = findById(bookRequest.getId());
            currentBookRequest.setTitle(bookRequest.getTitle());
            currentBookRequest.setDescription(bookRequest.getDescription());
            currentBookRequest.setAvailable(bookRequest.isAvailable());
            bookRequest = currentBookRequest;
        }
        return bookRequestRepo.save(bookRequest);
    }

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

    public BookRequestResponse findByIdResponse(long id) throws BusinessNotFound {
        Optional<BookRequest> bookRequest = bookRequestRepo.findById(id);
        if (bookRequest.isPresent()){
            return convertBookRequestToBookRequestReponse(bookRequest.get());
        }else {
            throw new BusinessNotFound("Book Request Not Found");
        }
    }

    public BookRequestResponse findByIdResponseMember(long id, long userId) throws BusinessNotFound, ForbiddenException {
        Optional<BookRequest> bookRequest = bookRequestRepo.findById(id);
        if (bookRequest.isPresent()){
            if(bookRequest.get().getUser().getId() == userId) {
                return convertBookRequestToBookRequestReponse(bookRequest.get());
            }else {
                throw new ForbiddenException("Access Denied");
            }
        }else {
            throw new BusinessNotFound("Book Request Not Found");
        }
    }

    public BookRequestResponse createBookRequest(long id, BookRequestRequest request){
        BookRequest bookRequest = new BookRequest();
        User user = userService.findById(id);
        bookRequest.setTitle(request.getTitle());
        bookRequest.setUser(user);
        bookRequest.setAvailable(false);
        bookRequest.setDescription(request.getDescription());
        return convertBookRequestToBookRequestReponse(save(bookRequest));
    }

    public BookRequestResponse updateBookRequest(long id,BookRequestRequest request) throws BusinessNotFound {
        BookRequest bookRequest = findById(id);
        if (bookRequest != null){
            bookRequest.setAvailable(request.isAvailable());
            bookRequest.setDescription(request.getDescription());
            return convertBookRequestToBookRequestReponse(save(bookRequest));
        }else {
            throw new BusinessNotFound("Book Request not found");
        }
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

    public void delete(long id) throws BusinessNotFound {
        Optional<BookRequest> bookRequest = bookRequestRepo.findById(id);
        if(bookRequest.isPresent()){
            bookRequestRepo.delete(bookRequest.get());
        }else {
            throw new BusinessNotFound("Book Request not found");
        }
    }

}
