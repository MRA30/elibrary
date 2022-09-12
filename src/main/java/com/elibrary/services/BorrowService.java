package com.elibrary.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.elibrary.dto.request.BorrowRequest;
import com.elibrary.dto.response.BorrowResponse;
import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.Borrow;
import com.elibrary.model.repos.BorrowRepo;

@Service
@Transactional
public class BorrowService {

    @Autowired
    private BorrowRepo borrowRepo;

    @Autowired
    private BookService bookService;

    public BorrowResponse conveBorrowTBorrowResponse(Borrow borrow){
        BorrowResponse response = new BorrowResponse();
        response.setBookId(borrow.getBookId());
        response.setBookId(borrow.getBookId());
        response.setUserId(borrow.getUserId());
        response.setBorrowDate(borrow.getBorrowDate());
        response.setReturnDate(borrow.getReturnDate());
        response.setReturned(borrow.isReturned());
        response.setPenalty(borrow.getPenalty());
        response.setDescription(borrow.getDescription());
        return response;
    }

    public Borrow convertBorrowRequestToBorrow(BorrowRequest request){
        Borrow borrow = new Borrow();
        borrow.setBookId(request.getBookId());
        borrow.setUserId(request.getUserId());
        borrow.setBorrowDate(request.getBorrowDate());
        borrow.setReturnDate(request.getReturnDate());
        // Custom date format
        SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd");  

        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(request.getBorrowDate().toString());
            d2 = format.parse(request.getReturnDate().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        } 
        long diff = d1.getTime() - d2.getTime();
        long day = TimeUnit.MILLISECONDS.toDays(diff);
        double fine = 0;
        if(day > 7){
            fine = (day - 7) * 5000;
        }
        double totalFine = fine + request.getPenalty();
        borrow.setPenalty(totalFine);
        borrow.setReturned(true);
        if(request.isBrokenorLost() == true){
            Book book = bookService.findById(request.getBookId());
            book.setQuantity(book.getQuantity() - 1);
            bookService.save(book);
        }
        String description = "Penalty for late return " + fine + ", and penalty for damaged/lost book " + request.getPenalty();
        borrow.setDescription(request.getDescription() +", " + description);
        return borrow;
    }

    public BorrowResponse addBorrow(BorrowRequest request){
        Borrow borrow = borrowRepo.save(convertBorrowRequestToBorrow(request));
        return conveBorrowTBorrowResponse(borrow);
    }

    public BorrowResponse updateBorrow(long id,BorrowRequest request){

        Borrow borrow = findById(id);
        convertBorrowRequestToBorrow(request);

        borrowRepo.save(borrow);
        return conveBorrowTBorrowResponse(borrow);
    }

    public Borrow findById(long id){
        Optional<Borrow> borrow = borrowRepo.findById(id);
        if(borrow.isPresent()){
            borrow.get();
        }
        return null;
    }

    public BorrowResponse findByIdResponse(long id){
        Optional<Borrow> borrow = borrowRepo.findById(id);
        if(borrow.isPresent()){
            return conveBorrowTBorrowResponse(borrow.get());
        } 
        return null;
    }
    
}
