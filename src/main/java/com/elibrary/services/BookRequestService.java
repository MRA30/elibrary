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

import com.elibrary.dto.request.BookRequestRequest;
import com.elibrary.dto.response.BookRequestReponse;
import com.elibrary.model.entity.BookRequest;
import com.elibrary.model.repos.BookRequestRepo;
import com.elibrary.model.specification.BookRequestSpecification;

@Service
@Transactional
public class BookRequestService {
    
    @Autowired
    private BookRequestRepo bookRequestRepo;

    @Autowired
    private BookRequestSpecification bookRequestSpecification;

    public BookRequestReponse convertBookRequestToBookRequestReponse(BookRequest request){
        BookRequestReponse bookRequestReponse = new BookRequestReponse();
        bookRequestReponse.setId(request.getId());

        bookRequestReponse.setUserId(request.getUserId());;
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

    public BookRequestReponse findByIdResponse(long id){
        Optional<BookRequest> bookRequest = bookRequestRepo.findById(id);
        if(bookRequest.isPresent()){
            return convertBookRequestToBookRequestReponse(bookRequest.get());
        }
        return null;
    }

    public BookRequestReponse createBookRequest(long id, BookRequestRequest request){
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle(request.getTitle());
        bookRequest.setUserId(id);
        bookRequest.setAvailable(false);
        bookRequest.setDescription(request.getDescription());
        return convertBookRequestToBookRequestReponse(bookRequestRepo.save(bookRequest));
    }

    public BookRequestReponse updateBookRequest(long id,BookRequestRequest request){
        BookRequest bookRequest = findByid(id);
        bookRequest.setAvailable(request.isAvailable());
        bookRequest.setDescription(request.getDescription());
        return convertBookRequestToBookRequestReponse(bookRequestRepo.save(bookRequest));
    }

    public Page<BookRequestReponse> searchBookRequest(String search, Integer page, Integer size, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<BookRequest> listBookRequest = bookRequestRepo.findAll(bookRequestSpecification.filterByTitle(search), pageable);
        int totalElements = (int) listBookRequest.getTotalElements();
        return new PageImpl<>(listBookRequest.stream().map(this::convertBookRequestToBookRequestReponse)
                                        .collect(Collectors.toList()), pageable, totalElements);
    }

    public Page <BookRequestReponse> filterById(String search, long userId, Integer page, Integer size, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<BookRequest> listBookRequest = bookRequestRepo.findAll(bookRequestSpecification.searchByTitleAndFilterByUserId(search, userId), pageable);
        int totalElements = (int) listBookRequest.getTotalElements();
        return new PageImpl<>(listBookRequest.stream().map(this::convertBookRequestToBookRequestReponse)
                                        .collect(Collectors.toList()), pageable, totalElements);
    }

    public void delete(long id){
        bookRequestRepo.deleteById(id);
    }
}
