package com.elibrary.controllers;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elibrary.Constans;
import com.elibrary.dto.request.LoginRequest;
import com.elibrary.dto.request.RegisterEmployeeRequest;
import com.elibrary.dto.request.RegisterMemberRequest;
import com.elibrary.dto.request.UpdateProfileRequest;
import com.elibrary.dto.response.LoginResponse;
import com.elibrary.dto.response.RegisterResponse;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.dto.response.UserResponse;
import com.elibrary.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register/employee")
    public ResponseEntity<ResponseData<RegisterResponse>> register(@Valid @RequestBody RegisterEmployeeRequest registerRequest, Errors errors) throws Exception{
        ResponseData<RegisterResponse> responseData = new ResponseData<>();
        try{
            if(errors.hasErrors()){
                for (ObjectError error : errors.getAllErrors()) {
                    responseData.getMessages().add(error.getDefaultMessage());
                }
                responseData.setStatus(false);
                responseData.setPayload(null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
            }
            RegisterResponse registerResponse = userService.registerEmployee(registerRequest);
            responseData.setStatus(true);
            responseData.setPayload(registerResponse);
            responseData.getMessages().add("Register Success");
            return ResponseEntity.ok(responseData);
        }catch (Exception ex){
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }
    }

    @PostMapping("/register/member")
    public ResponseEntity<ResponseData<RegisterResponse>> registermember(@Valid @RequestBody RegisterMemberRequest registerRequest, Errors errors) throws Exception{
        ResponseData<RegisterResponse> responseData = new ResponseData<>();
        try{
            if(errors.hasErrors()){
                for (ObjectError error : errors.getAllErrors()) {
                    responseData.getMessages().add(error.getDefaultMessage());
                }
                responseData.setStatus(false);
                responseData.setPayload(null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
            }
            RegisterResponse registerResponse = userService.registerMember(registerRequest);
            responseData.setStatus(true);
            responseData.setPayload(registerResponse);
            responseData.getMessages().add("Register Success");
            return ResponseEntity.ok(responseData);
        }catch (Exception ex){
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseData<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest, 
                                                            HttpServletResponse response, Errors errors) throws Exception {
        ResponseData<LoginResponse> responseData = new ResponseData<>();
        if(errors.hasErrors()){
            for (ObjectError error : errors.getAllErrors()) {
                responseData.getMessages().add(error.getDefaultMessage());
            }
            responseData.setStatus(false);
            responseData.setPayload(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }
        try {
            
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            String jwtToken =  userService.login(loginRequest.getEmail());
            Cookie newCookie = new Cookie(Constans.ACCESS_TOKEN, jwtToken);
            newCookie.setHttpOnly(true);
            newCookie.setPath("/");
            newCookie.setSecure(true);
            newCookie.setMaxAge(Constans.COOKIE_VALID);
            response.addCookie(newCookie);

            LoginResponse loginResponse = userService.findByEmail(loginRequest.getEmail());
            responseData.setStatus(true);
            responseData.setPayload(loginResponse);
            responseData.getMessages().add("Login Success");
            return ResponseEntity.ok(responseData);

        } catch (Exception ex) {
            responseData.setStatus(false);
            responseData.setPayload(null);
            responseData.getMessages().add("invalid Username or Password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ResponseData<UserResponse>> profileUser(){
        ResponseData<UserResponse> response = new ResponseData<>();
        try{
            Long userId = userService.getUser().getId();
            UserResponse userResponse = userService.profile(userId);
            response.setStatus(true);
            response.setPayload(userResponse);
            response.getMessages().add("Success");
            return ResponseEntity.ok(response);
        }catch(Exception ex){
            response.setStatus(false);
            response.setPayload(null);
            response.getMessages().add(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseData<UserResponse>> updateUser(@Valid @RequestBody UpdateProfileRequest updateProfileRequest, Errors errors){
        Long userId = userService.getUser().getId();
        ResponseData<UserResponse> response = new ResponseData<>();
        if(errors.hasErrors()){
            for (ObjectError error : errors.getAllErrors()) {
                response.getMessages().add(error.getDefaultMessage());
            }
            response.setStatus(false);
            response.setPayload(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }try{
            if(userService.existsByEmail(updateProfileRequest.getEmail()) 
                && userId != userService.findByEmail(updateProfileRequest.getEmail()).getId()){
                response.setStatus(false);
                response.setPayload(null);
                response.getMessages().add("email already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if(userService.existsBynoHp(updateProfileRequest.getNoHp())
                && userId != userService.findBynoHp(updateProfileRequest.getNoHp()).getId()){
                response.setStatus(false);
                response.setPayload(null);
                response.getMessages().add("no hp already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            UserResponse userResponse = userService.updateUser(userId, updateProfileRequest);
            response.setStatus(true);
            response.setPayload(userResponse);
            response.getMessages().add("Success");
            return ResponseEntity.ok(response);
        }catch(Exception ex){
            response.setStatus(false);
            response.setPayload(null);
            response.getMessages().add(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
}
