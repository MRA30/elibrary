package com.elibrary.services;

import com.elibrary.Constans;
import com.elibrary.Exception.BorrowException;
import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.ForbiddenException;
import com.elibrary.config.OneSignalConfig;
import com.elibrary.dto.request.BorrowRequestAdd;
import com.elibrary.dto.request.BorrowRequestUpdate;
import com.elibrary.dto.response.BorrowOvertimeResponse;
import com.elibrary.dto.response.BorrowResponse;
import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.Borrow;
import com.elibrary.model.entity.User;
import com.elibrary.model.repos.BorrowRepo;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.RequiredArgsConstructor;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
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
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRepo borrowRepo;

    private final BookService bookService;

    private final UserService userService;

    private final OneSignalConfig oneSignalConfig;

    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

    private Borrow save(Borrow borrow) {
        if(borrow.getId() != null){
            Borrow currentBorrow = findById(borrow.getId());
            currentBorrow.setBook(borrow.getBook());
            currentBorrow.setUser(borrow.getUser());
            currentBorrow.setBorrowDate(borrow.getBorrowDate());
            currentBorrow.setReturnDate(borrow.getReturnDate());
            currentBorrow.setReturned(borrow.isReturned());
            currentBorrow.setPenalty(borrow.getPenalty());
            currentBorrow.setDescription(borrow.getDescription());
            borrow = currentBorrow;
        }
        return borrowRepo.save(borrow);
    }

    public int daydiff(Date borrowDate) {
        Date now = new Date();
        int daydiff = Days.daysBetween(
                new LocalDate(borrowDate.getTime() + 7 * 24 * 60 * 60 * 1000),
                new LocalDate(now.getTime())
        ).getDays();
        return Math.max(daydiff, 0);
    }

    public Borrow findById(long id) {
        Optional<Borrow> borrow = borrowRepo.findById(id);
        return borrow.orElse(null);
    }

    public BorrowResponse findByIdResponse(long id) throws BusinessNotFound {
       Optional<Borrow> borrow = borrowRepo.findById(id);
       if(borrow.isPresent()){
           return convertBorrowToBorrowResponse(borrow.get());
       }
       throw new BusinessNotFound("Borrow not found");
    }

    public BorrowResponse findByIdResponseMember(long id, long userId) throws BusinessNotFound, ForbiddenException {
        Optional<Borrow> borrow = borrowRepo.findById(id);
        if(borrow.isPresent()){
            if(borrow.get().getUser().getId() != userId) {
                throw new ForbiddenException("Access denied");
            }
            return convertBorrowToBorrowResponse(borrow.get());
        }
        throw new BusinessNotFound("Borrow not found");
    }

    public boolean checkDuplicateValue(List<BorrowRequestAdd> borrowRequestAdd){
        List<Long> listBookId = new ArrayList<>();
        for (BorrowRequestAdd requestAdd : borrowRequestAdd) {
            listBookId.add(requestAdd.getBookId());
        }
        int distinct = (int) listBookId.stream().distinct().count();
        return distinct != listBookId.size();
    }

    public List<String> checkUserBorrows(List<BorrowRequestAdd> borrowRequestAdds){
        List<String> messagesList = new ArrayList<>();
        Long userId = borrowRequestAdds.get(0).getUserId();
        for (BorrowRequestAdd borrowRequestAdd : borrowRequestAdds) {
            if(checkBorrow(borrowRequestAdd.getBookId(), userId)){
                messagesList.add("Book with id " + borrowRequestAdd.getBookId() + " is borrowed by user with id " + userId);
            }
        }
        return messagesList;
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

    public BorrowResponse convertBorrowToBorrowResponse(Borrow borrow){
        return new BorrowResponse(
            borrow.getId(),
            borrow.getBook().getId(),
            borrow.getBook().getTitle(),
            borrow.getUser().getId(),
            borrow.getUser().getFirstName() + " " + borrow.getUser().getLastName(),
            borrow.getBorrowDate().toString(),
            borrow.getReturnDate().toString(),
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

    public BorrowOvertimeResponse convertBorrowToBorrowOvertimeResponse(Borrow borrow){
        int days_difference = daydiff(borrow.getBorrowDate());
        return new BorrowOvertimeResponse(
            borrow.getId(),
            borrow.getBook().getId(),
            borrow.getBook().getTitle(),
            borrow.getUser().getId(),
            borrow.getUser().getFirstName() + " " + borrow.getUser().getLastName(),
            borrow.getBorrowDate().toString(),
            borrow.getReturnDate().toString(),
            days_difference,
            borrow.getDescription()
        );
    }

    public List<BorrowResponse> addBorrows(List<BorrowRequestAdd> request) throws BorrowException {
        for (BorrowRequestAdd borrowRequestAdd : request) {
            if(!bookService.existsById(borrowRequestAdd.getBookId())){
                throw new BorrowException("Book " + bookService.findById(borrowRequestAdd.getBookId()).getTitle() + " doesn't exist");
            }
        }
        if(!userService.existsById(request.get(0).getUserId())){
            String fullName = userService.findById(request.get(0).getUserId()).getFirstName() + " " + userService.findById(request.get(0).getUserId()).getLastName();
            throw new BorrowException("User " + fullName + " doesn't exist");
        }
        if(checkDuplicateValue(request)){
            throw new BorrowException("User can't borrow same book");
        }
        if(request.size() > 3){
            throw new BorrowException("User can't borrow more than 3 books");
        }
        List<String> message = checkUserBorrows(request);
        if (message.size() > 0) {
            throw new BorrowException(String.join(", ", message));
        }
        if(countUserBorrows(request.get(0).getUserId()) >= 3){
            throw new BorrowException("User can't borrow more than 3 books");
        }
        for(BorrowRequestAdd borrowRequestAdd : request){
            Book book = bookService.findById(borrowRequestAdd.getBookId());
            if(countBookBorrow(borrowRequestAdd.getBookId()) - bookService.findById(borrowRequestAdd.getBookId()).getQuantity() == 0){
                throw new BorrowException("Book " + book.getTitle() + " is not available now");
            }
        }
        if (countUserBorrows(request.get(0).getUserId()) + request.size() > 3){
            throw new BorrowException("User can only borrow " + (3 - countUserBorrows(request.get(0).getUserId())) + " books");
        }
        List<Borrow> borrow = borrowRepo.saveAll(request.stream().map(this::convertBorrowRequestToBorrowAdd).collect(Collectors.toList()));
        return borrow.stream().map(this::convertBorrowToBorrowResponse).collect(Collectors.toList());
    }

    public BorrowResponse updateBorrow(long id, BorrowRequestUpdate request) throws BusinessNotFound {
        Borrow borrow = findById(id);
        if(borrow == null){
            throw new BusinessNotFound("Borrow not found");
        }
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
        return convertBorrowToBorrowResponse(save(borrow));
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


    @Scheduled(cron = "* 0 8 * * *")
//    @Scheduled(cron = "30 * * * * *")
    public void borrowOvertimeNotify() {
        List<Borrow> borrows = borrowRepo.findBorrowsOvertime();
        List<BorrowOvertimeResponse> borrowOvertimeResponses =  borrows.stream().map(this::convertBorrowToBorrowOvertimeResponse).collect(Collectors.toList());
        borrowOvertimeResponses.forEach(borrowOvertimeResponse -> {
            User user = userService.findById(borrowOvertimeResponse.getUserId());
            String email = user.getEmail();
            String name = user.getFirstName() + " " + user.getLastName();
            String message = "";
            if(borrowOvertimeResponse.getDaysOvertime() > 0){
                message = "You have borrowed the book " + borrowOvertimeResponse.getBookTitle() + " on " + borrowOvertimeResponse.getBorrowDate() + ".\n" +
                        "You late to return the book on " + borrowOvertimeResponse.getReturnDate()+ ".\n" +
                        "You have been fined Rp. " + Constans.PENALTY * borrowOvertimeResponse.getDaysOvertime() + " for " + borrowOvertimeResponse.getDaysOvertime() + " days.\n" +
                        "Thank you for visiting our library.";
            }else{
                message = "You have borrowed the book " + borrowOvertimeResponse.getBookTitle() + " on " + borrowOvertimeResponse.getBorrowDate() + ".\n" +
                        "You have to return the book today on " + borrowOvertimeResponse.getReturnDate() + " or you will be fined Rp. " + Constans.PENALTY + "/day.\n" +
                        "Thank you for visiting our library.";
            }
            try {
                oneSignalConfig.pushNotifyBorrow(email, name, message);
            } catch (UnirestException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
