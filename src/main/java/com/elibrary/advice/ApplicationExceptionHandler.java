package com.elibrary.advice;

import com.elibrary.Constans;
import com.elibrary.Exception.*;
import com.elibrary.dto.response.ResponseData;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseData<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });
        return new ResponseData<>(false, errorMap, null);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {BusinessNotFound.class})
    public ResponseData<Map<String, String>> handleBusinessNotFound(BusinessNotFound ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put(Constans.MESSAGE, ex.getMessage());
        return new ResponseData<>(false, errorMap, null);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {UserException.class})
    public ResponseData<Map<String, String>> handleRegisterEmployeeException(UserException ex) {
        Map<String, String> errorMap = new HashMap<>();
        if(ex.getMessage().toLowerCase().contains("email")) {
            errorMap.put("email", ex.getMessage());
        } else if(ex.getMessage().toLowerCase().contains("username")) {
            errorMap.put("username", ex.getMessage());
        }else if(ex.getMessage().toLowerCase().contains("no hp")) {
            errorMap.put("noHp", ex.getMessage());
        }else if(ex.getMessage().toLowerCase().contains("number identity")) {
            errorMap.put("numberIdentity", ex.getMessage());
        }
        return new ResponseData<>(false, errorMap, null);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {CategoryException.class})
    public ResponseData<Map<String, String>> handleCategoryException(CategoryException ex) {
        Map<String, String> errorMap = new HashMap<>();
        if(ex.getMessage().toLowerCase().contains("category")) {
            errorMap.put("category", ex.getMessage());
        }
        return new ResponseData<>(false, errorMap, null);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {Exception.class})
    public ResponseData<Map<String, String>> handleException(Exception ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put(Constans.MESSAGE, ex.getMessage());
        return new ResponseData<>(false, errorMap, null);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {BorrowException.class})
    public ResponseData<Map<String, String>> handleBorrowException(BorrowException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put(Constans.MESSAGE, ex.getMessage());
        return new ResponseData<>(false, errorMap, null);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = {ForbiddenException.class})
    public ResponseData<Map<String, String>> handleForbiddenException(ForbiddenException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put(Constans.MESSAGE, ex.getMessage());
        return new ResponseData<>(false, errorMap, null);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {UnirestException.class})
    public ResponseData<Map<String, String>> handleUnirestException(UnirestException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put(Constans.MESSAGE, ex.getMessage());
        return new ResponseData<>(false, errorMap, null);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {IOException.class})
    public ResponseData<Map<String, String>> handleIOException(IOException ex){
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put(Constans.MESSAGE, ex.getMessage());
        return new ResponseData<>(false, errorMap, null);
    }
}
