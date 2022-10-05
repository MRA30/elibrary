package com.elibrary.config;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OneSignalConfig {

    private final String APP_ID = "f7e2a367-cb10-4057-a234-c532ff8120b3";
    private final String KEY_ID = "MmFkMjcwYmUtM2JlMC00N2UwLWJhNDEtNTJiMGM2ZTU1MmI1";

    public void pushNotifyBorrow(String email, String name, String message) throws UnirestException {
        String url = "https://onesignal.com/api/v1/notifications";
        Unirest.post(url)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Basic " + KEY_ID)
                .body("{"
                        + "\"app_id\": \"" + APP_ID + "\","
                        + "\"headings\": {\"en\": \"Hello " + name + "\"},"
                        + "\"contents\": {\"en\": \"" + message + "\"},"
                        + "\"include_external_user_ids\": [\"" + email + "\"]"
                        + "}")
                .asJson()
                .getBody();

    }

    public void pushNotifyMorning(String email, String name, String message) throws UnirestException {
        String url = "https://onesignal.com/api/v1/notifications";
        Unirest.post(url)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Basic " + KEY_ID)
                .body("{"
                        + "\"app_id\": \"" + APP_ID + "\","
                        + "\"headings\": {\"en\": \"Hello " + name + "\"},"
                        + "\"contents\": {\"en\": \"" + message + "\"},"
                        + "\"include_external_user_ids\": [\"" + email + "\"]"
                        + "}")
                .asJson()
                .getBody();

    }
}
