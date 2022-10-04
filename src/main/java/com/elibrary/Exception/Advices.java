package com.elibrary.Exception;

import com.elibrary.dto.response.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class Advices {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<?>> handleInvalidArgument(MethodArgumentNotValidException ex) {
        List<String> messages = new ArrayList<>();
        for (var error : ex.getBindingResult().getAllErrors()) {
            messages.add(error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messages, null));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ResponseEntity<ResponseData<?>> handleUnauthorizedException(HttpClientErrorException.Unauthorized ex) {
        List<String> message = new ArrayList<>();
        message.add(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseData<>(false, message, null));
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ResponseEntity<ResponseData<?>> handleForbiddenException(HttpClientErrorException.Forbidden ex) {
        List<String> message = new ArrayList<>();
        message.add(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseData<>(false, message, null));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<?>> handleInternalServerError(Exception ex) {
        List<String> message = new ArrayList<>();
        message.add(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, message, null));
    }
}
