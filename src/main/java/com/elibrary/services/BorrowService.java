package com.elibrary.services;

import com.elibrary.Constans;
import com.elibrary.config.OneSignalConfig;
import com.elibrary.dto.request.BorrowRequestAdd;
import com.elibrary.dto.request.BorrowRequestUpdate;
import com.elibrary.dto.response.BorrowOvertimeResponse;
import com.elibrary.dto.response.BorrowResponse;
import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.Borrow;
import com.elibrary.model.entity.User;
import com.elibrary.model.repos.BorrowRepo;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowService {


    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private BorrowRepo borrowRepo;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private OneSignalConfig oneSignalConfig;

    public int daydiff(Date borrowDate) {
        Date now = new Date();
        int daydiff = Days.daysBetween(
                new LocalDate(borrowDate.getTime() + 7 * 24 * 60 * 60 * 1000),
                new LocalDate(now.getTime())
        ).getDays();
        if(daydiff > 0) {
            return daydiff;
        }
        return 0;
    }

    public Borrow findById(long id) {
        Optional<Borrow> borrow = borrowRepo.findById(id);
        if(borrow.isPresent()){
            return borrow.get();
        }
        return null;
    }

    public BorrowResponse findByIdResponse(long id){
       Optional<Borrow> borrow = borrowRepo.findById(id);
       if(borrow.isPresent()){
           return convertBorrowToBorrowResponse(borrow.get());
       }
       return null;
    }

    public String fineOrPenalty(long datediff, double penalty){
        if(datediff > 0 && penalty > 0){
            return  "Borrow returned successfully and you have to pay Rp. " + datediff * Constans.PENALTY + " for late return" +
            " and penalty fee is Rp. " + penalty + " for damaged or lost book";
        }else if(datediff > 0 && penalty == 0) {
            return "Borrow returned successfully and you have to pay Rp. " + datediff * Constans.PENALTY + " for late return";
        }else if(penalty > 0){
            return "Borrow returned successfully and penalty fee is Rp. " + penalty + " for damaged or lost book";
        }else{
            return "Borrow returned successfully";
        }
    }

    public List<String> checkBookBorrowsAndQuantity(BorrowRequestAdd request){
        List<String> messagesList = new ArrayList<>();
        if(checkBorrow(request.getBookId(), request.getUserId())){
            messagesList.add("User already borrow this book");
        }
        Book book = bookService.findById(request.getBookId());
        if(book.getQuantity() - countBookBorrow(request.getBookId()) <= 0){
            messagesList.add("Book " + book.getTitle() + " is not available now");
        }
        if(countUserBorrows(request.getUserId()) >= 3){
            messagesList.add("User can only borrow 3 books");
        }
        return messagesList;
    }

    public BorrowResponse convertBorrowToBorrowResponse(Borrow borrow){
        return new BorrowResponse(
            borrow.getId(),
            borrow.getBook().getId(),
            borrow.getBook().getTitle(),
            borrow.getUser().getId(),
            borrow.getUser().getFirstName() + " " + borrow.getUser().getLastName(),
            borrow.getBorrowDate(),
            borrow.getReturnDate(),
            borrow.isReturned(),
            borrow.getPenalty(),
            borrow.getDescription()
        );
        
    }

    public Borrow convertBorrowRequestToBorrowAdd(BorrowRequestAdd borrowRequest) {
        Book book = bookService.findById(borrowRequest.getBookId());
        User user = userService.findById(borrowRequest.getUserId());

        Date borrowDate = new Date();
        Date returnDate = new Date(borrowDate.getTime() + 7 * 24 * 60 * 60 * 1000);
        return new Borrow(
            book,
            user,
            java.sql.Date.valueOf(format.format(borrowDate)),
            java.sql.Date.valueOf(format.format(returnDate)),
            false,
            0,
            borrowRequest.getDescription()
        );
    }

//    public Borrow convertBorrowRequestToBorrowToUpdate(long id, BorrowRequestUpdate request) {
//
//        Borrow borrow = findById(id);
//        double fine = 0;
//        Date now = new Date();
//        long sevendays = 7 * 24 * 60 * 60 * 1000;
//        long diff = Math.abs(now.getTime() - (borrow.getBorrowDate().getTime() + sevendays));
//        long days_difference = (diff / (1000*60*60*24)) % 365;
//        if (days_difference > 0) {
//            fine = days_difference * 5000;
//        }
//        double totalFine = fine + request.getPenalty();
//        if (request.isLostBroken()) {
//            Book bookBrokenOrLost = bookService.findById(borrow.getBook().getId());
//            bookBrokenOrLost.setQuantity(bookBrokenOrLost.getQuantity() - 1);
//            bookService.save(bookBrokenOrLost);
//        }
//        String description = borrow.getDescription() + ", " + request.getDescription() + ", " + "Penalty for late return " + fine + ", and penalty for damaged/lost book " + request.getPenalty();
//        borrow.setReturned(request.isReturned());
//        borrow.setPenalty(totalFine);
//        borrow.setDescription(description);
//        return borrow;
//    }

    public BorrowOvertimeResponse convertBorrowToBorrowOvertimeResponse(Borrow borrow){
        int days_difference = daydiff(borrow.getBorrowDate());
        return new BorrowOvertimeResponse(
            borrow.getId(),
            borrow.getBook().getId(),
            borrow.getBook().getTitle(),
            borrow.getUser().getId(),
            borrow.getUser().getFirstName() + " " + borrow.getUser().getLastName(),
            borrow.getBorrowDate(),
            borrow.getReturnDate(),
            days_difference,
            borrow.getDescription()
        );
    }

    public BorrowResponse addBorrow(BorrowRequestAdd request){
        Borrow borrow = borrowRepo.save(convertBorrowRequestToBorrowAdd(request));
        return convertBorrowToBorrowResponse(borrow);
    }

    public BorrowResponse updateBorrow(long id, BorrowRequestUpdate request){
        System.out.println("id " + id);
        Borrow borrow = findById(id);
        System.out.println(borrow);
        double fine = 0;
        Date now = new Date();
        long diff = now.getTime() - borrow.getReturnDate().getTime();
        long days_difference = (diff / (1000*60*60*24)) % 365;
        if (days_difference > 0) {
            fine = days_difference * Constans.PENALTY;
        }
        double totalFine = fine + request.getPenalty();
        System.out.println(days_difference);
        System.out.println(request.isLostOrDamage());
        if (request.isLostOrDamage()) {
            Book bookBrokenOrLost = bookService.findById(borrow.getBook().getId());
            bookBrokenOrLost.setQuantity(bookBrokenOrLost.getQuantity() - 1);
            bookService.save(bookBrokenOrLost);
        }
        borrow.setReturned(request.isReturned());
        System.out.println(request.isReturned());
        if(borrow.isReturned()){
            borrow.setReturnDate(java.sql.Date.valueOf(format.format(new Date())));
            borrow.setPenalty(totalFine);
        }
        borrow.setDescription(request.getDescription());
        borrowRepo.save(borrow);
        return convertBorrowToBorrowResponse(borrow);
    }

    public Page<BorrowResponse> findAllBorrows(String search, boolean returned, int size, int page, String sortBy, String direction){
        Pageable pageable;
        if(direction.equals("desc")){
            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        } else{
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        }
        Page<Borrow> borrows = borrowRepo.findAllBorrows(search.toLowerCase(), returned, pageable);
        int totalElements = borrows.getNumberOfElements();
        return new PageImpl<>(borrows.stream().map(this::convertBorrowToBorrowResponse)
                .collect(Collectors.toList()), pageable, totalElements);
    }

    public Page<BorrowResponse> filterBorrowsById(Principal principal, boolean returnStatus, int size, int page, String sortBy, String direction){
        long userId = userService.getProfile(principal).getId();
        Pageable pageable;
        if(direction.equals("desc")){
            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        } else{
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        }
        Page<Borrow> borrows = borrowRepo.filterBorrowsByUserId(userId, returnStatus, pageable);
        int totalElement = (int) borrows.getTotalElements();
        return new PageImpl<>(borrows.getContent()
                .stream().map(this::convertBorrowToBorrowResponse)
                .collect(Collectors.toList()), pageable, totalElement);
    }

    public Page<BorrowOvertimeResponse> getBorrowOvertime(int page, int size, String sortBy, String direction){
        Pageable pageable;
        if(direction.equals("desc")){
            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        } else{
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        }
        Page<Borrow> borrows = borrowRepo.findAllBorrowsOvertime(pageable);
        int totalElement = (int) borrows.getTotalElements();
        return new PageImpl<>(borrows.getContent()
                .stream().map(this::convertBorrowToBorrowOvertimeResponse)
                .collect(Collectors.toList()), pageable, totalElement);
    }

    public int countBookBorrow(long id){
        return borrowRepo.countBookBorrow(id);
    }

    public int countUserBorrows(long id){
        return borrowRepo.findBorrowsByUserId(id).size();
    }
    public boolean checkBorrow(Long bookId, Long userId) {
        return borrowRepo.checkBorrow(bookId, userId) >= 1;
    }

    public boolean existsById(long id){
        return borrowRepo.existsById(id);
    }


}
