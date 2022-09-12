package com.elibrary.services;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.elibrary.dto.request.BookRequestRequest;
import com.elibrary.dto.response.BookRequestResponse;
import com.elibrary.model.entity.BookRequest;
import com.elibrary.model.entity.User;
import com.elibrary.model.repos.BookRequestRepo;

@Service
@Transactional
public class BookRequestService {
    
    @Autowired
    private BookRequestRepo bookRequestRepo;

    @Autowired
    private UserService userService;

    public BookRequestResponse convertBookRequestToBookRequestReponse(long id, BookRequest request){
        BookRequestResponse bookRequestReponse = new BookRequestResponse();
        bookRequestReponse.setId(request.getId());

        User user = userService.findById(id);
        bookRequestReponse.setUserId(request.getUserId());
        bookRequestReponse.setName(user.getName());;
        bookRequestReponse.setTitle(request.getTitle());
        bookRequestReponse.setAvailable(request.isAvailable());
        bookRequestReponse.setDescription(request.getDescription());
        return bookRequestReponse;
    }

    public BookRequest findByid(long id){
        Optional<BookRequest> bookRequest = bookRequestRepo.findById(id);
        if(bookRequest.isPresent()){
            return bookRequest.get();
        }
        return null;
    }

    public BookRequestResponse createBookRequest(long id, BookRequestRequest request){
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle(request.getTitle());
        bookRequest.setUserId(id);
        bookRequest.setAvailable(false);
        bookRequest.setDescription(request.getDescription());
        return convertBookRequestToBookRequestReponse(id,bookRequestRepo.save(bookRequest));
    }

    public BookRequestResponse updateBookRequest(long id,BookRequestRequest request){
        BookRequest bookRequest = findByid(id);
        long userId = request.getUserId();
        bookRequest.setAvailable(request.isAvailable());
        bookRequest.setDescription(request.getDescription());
        return convertBookRequestToBookRequestReponse(userId, bookRequestRepo.save(bookRequest));
    }

    public Page<BookRequestResponse> searchBookRequest(String search, Integer page, Integer size, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<BookRequestResponse> listBookRequest = bookRequestRepo.findByTitleContainingIgnoreCase(search.toLowerCase(), pageable);
        return listBookRequest;
    }

    public Page <BookRequestResponse> filterById(String search, long userId, Integer page, Integer size, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<BookRequestResponse> listBookRequest = bookRequestRepo.findByTitleContainingIgnoreCaseMember(search.toLowerCase(), userId, pageable);
        return listBookRequest;
    }

    public void delete(long id){
        bookRequestRepo.deleteById(id);
    }

    public BookRequestResponse filterById(long id){
        BookRequestResponse bRequestReponse = bookRequestRepo.findByIdBookRequest(id);
        return bRequestReponse;
    }
}
