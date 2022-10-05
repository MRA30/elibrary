package com.elibrary.config;

import com.elibrary.model.entity.User;
import com.elibrary.services.UserService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduling.enabled", matchIfMissing = true)
public class SchedulingConfig {

    @Autowired
    private OneSignalConfig oneSignalConfig;

    @Autowired
    private UserService userService;


//    @Scheduled(cron = "0 0 7 * * *")
    @Scheduled(cron = "15 * * * * *")
    public void pushNotifyMorning() throws UnirestException {
    List<User> users = userService.findUserEnabled();
        for (User user : users) {
            oneSignalConfig.pushNotifyMorning(user.getEmail(), user.getFirstName() +  " " + user.getLastName(), "Good morning, have a nice day!");
        }
    }

}
