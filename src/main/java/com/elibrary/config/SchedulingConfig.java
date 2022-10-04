package com.elibrary.config;

import com.elibrary.Constans;
import com.elibrary.dto.response.BorrowOvertimeResponse;
import com.elibrary.model.entity.Borrow;
import com.elibrary.model.entity.User;
import com.elibrary.model.repos.BorrowRepo;
import com.elibrary.services.BorrowService;
import com.elibrary.services.UserService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduling.enabled", matchIfMissing = true)
public class SchedulingConfig {

    @Autowired
    private OneSignalConfig oneSignalConfig;

    @Autowired
    private BorrowRepo borrowRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowService borrowService;


//    @Scheduled(cron = "* 0 8 * * *")
    @Scheduled(cron = "30 * * * * *")
    public void borrowOvertimeNotify() {
        List<Borrow> borrows = borrowRepo.findBorrowsOvertime();
        List<BorrowOvertimeResponse> borrowOvertimeResponses =  borrows.stream().map(borrow -> borrowService.convertBorrowToBorrowOvertimeResponse(borrow)).collect(Collectors.toList());
        borrowOvertimeResponses.forEach(borrowOvertimeResponse -> {
            User user = userService.findById(borrowOvertimeResponse.getUserId());
            String email = user.getEmail();
            String name = user.getFirstName() + " " + user.getLastName();
            String message = "";
            if(borrowOvertimeResponse.getDaysOvertime() < 0){
                message = "You have borrowed the book " + borrowOvertimeResponse.getBookTitle() + " on " + borrowOvertimeResponse.getBorrowDate() + ".\n" +
                        "You have to return the book tomorrow on " + borrowOvertimeResponse.getReturnDate()+ " or you will be fined Rp. " + Constans.PENALTY + "/day.\n" +
                        "Thank you for visiting our library.";
            }else if(borrowOvertimeResponse.getDaysOvertime() > 0){
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
                oneSignalConfig.pushNotifBorrow(email, name, message);
            } catch (UnirestException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
