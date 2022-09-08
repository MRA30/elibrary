package com.elibrary.services;

import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.elibrary.dto.request.RegisterEmployeeRequest;
import com.elibrary.dto.request.RegisterMemberRequest;
import com.elibrary.dto.request.UpdateImageRequest;
import com.elibrary.dto.request.UpdatePasswordRequest;
import com.elibrary.dto.request.UpdateProfileRequest;
import com.elibrary.dto.response.LoginResponse;
import com.elibrary.dto.response.RegisterResponse;
import com.elibrary.dto.response.UserResponse;
import com.elibrary.model.entity.Gender;
import com.elibrary.model.entity.User;
import com.elibrary.model.entity.UserRole;
import com.elibrary.model.repos.UserRepo;
import com.elibrary.utils.JwtUtil;

@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // function for generate random number
    public String generateRandomNumber(int length) {
        String number = "";
        for (int i = 0; i < length; i++) {
            number += (int) (Math.random() * 10);
        }
        return number;
    }

    private Principal principal(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public User getUser(){
        return userRepo.findByEmail(principal().getName());
    }

    public Long getId(){
        return userRepo.findByEmail(principal().getName()).getId();
    }

    public RegisterResponse convertUserToResponse(User user){
        RegisterResponse response = new RegisterResponse(
            user.getId(),
            user.getNumberIdentity(),
            user.getName(),
            user.getGender(),
            user.getNoHp(),
            user.getAddress(),
            user.getEmail(),
            user.getImage()
        );
        return response;
    }

    public UserResponse convertUserToUserResponse(User user){
        UserResponse response = new UserResponse(
            user.getId(),
            user.getNumberIdentity(),
            user.getName(),
            user.getGender(),
            user.getNoHp(),
            user.getAddress(),
            user.getEmail(),
            user.getUserRole(),
            user.getImage(),
            user.getBorrows(),
            user.getBookRequests()
        );
        return response;
    }

    public User findById(long id){
        Optional<User> user = userRepo.findById(id);
        if(user.isPresent()){
            return user.get();
        }
        return null;
    }

    public UserResponse findByIdUserResponse(long id){
        Optional<User> user = userRepo.findById(id);
        if(user.isPresent()){
            return convertUserToUserResponse(user.get());
        }
        return null;
    }

    public RegisterResponse registerEmployee(RegisterEmployeeRequest request) throws IOException {

        String emailLowerCase = request.getEmail().toLowerCase();
        request.setEmail(emailLowerCase);
        String encodePassword = bCryptPasswordEncoder.encode(request.getPassword());
        request.setPassword(encodePassword);

        String numberIdentity = "EM" + request.getNumberIdentity();
        request.setNumberIdentity(numberIdentity);


        User user = new User();
        user.setNumberIdentity(request.getNumberIdentity());
        user.setName(request.getName());
        String gender = request.getGender();
        switch (gender) {
            case "Male":
                user.setGender(Gender.Male);
                break;
        
            default: user.setGender(Gender.Female);
                break;
        }
        user.setNoHp(request.getNoHp());
        user.setAddress(request.getAddress());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setUserRole(UserRole.EMPLOYEE);
        user.setImage(request.getImage());
        
        userRepo.save(user);

        return convertUserToResponse(user);
    }

    public RegisterResponse registerMember(RegisterMemberRequest request) throws IOException {

        String emailLowerCase = request.getEmail().toLowerCase();
        request.setEmail(emailLowerCase);
        String encodePassword = bCryptPasswordEncoder.encode(request.getPassword());
        request.setPassword(encodePassword);

        String generateRandomNumber = generateRandomNumber(3);
        String numberIdentity = "ME" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + generateRandomNumber;

        User user = new User();
        user.setNumberIdentity(numberIdentity);
        user.setName(request.getName());
        String gender = request.getGender();
        switch (gender) {
            case "Male":
                user.setGender(Gender.Male);
                break;
        
            default: user.setGender(Gender.Female);
                break;
        }
        user.setNoHp(request.getNoHp());
        user.setAddress(request.getAddress());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setUserRole(UserRole.MEMBER);
        user.setImage(request.getImage());
        
        userRepo.save(user);

        return convertUserToResponse(user);
    }

    public String login(String email) throws IOException {
        User user = userRepo.findByEmail(email);
        return jwtUtil.generateToken(user);      
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email);
        if(user == null){
            return (UserDetails) new UsernameNotFoundException(
                    String.format("email %s not found", email));
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

    public LoginResponse findByEmail(String email){
        User user = userRepo.findByEmail(email);
        if(user != null){
            LoginResponse response = new LoginResponse();
            response.setId(user.getId());
            response.setNumberIdentity(user.getNumberIdentity());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            return response;
        }
        return null;
    }

    public UserResponse updateUser(long id, UpdateProfileRequest request) throws IOException {
        User user  = findById(id);
        user.setName(request.getName());
        String gender = request.getGender();
        switch (gender) {
            case "Male":
                user.setGender(Gender.Male);
                break;
        
            default: user.setGender(Gender.Female);
                break;
        }
        user.setNoHp(request.getNoHp());
        user.setAddress(request.getAddress());
        user.setEmail(request.getEmail());
        userRepo.save(user);
        return convertUserToUserResponse(user);
    }

    public RegisterResponse updatePassword(long id, UpdatePasswordRequest request){
            User user = findById(id);
            String encodePassword = bCryptPasswordEncoder.encode(request.getNewPassword());
            user.setPassword(encodePassword);
            userRepo.save(user);
            return convertUserToResponse(user);
    }

    public RegisterResponse updateImage(long id, UpdateImageRequest request){
        User user = findById(id);
            user.setImage(request.getImage());
            userRepo.save(user);
            return convertUserToResponse(user);
    }

    public UserResponse profile(long id){
        User user = findById(id);
        return convertUserToUserResponse(user);
    }

    public boolean existsByEmail(String email){
        return userRepo.existsByEmail(email);
    }

    public boolean existsBynoHp(String noHp){
        return userRepo.existsBynoHp(noHp);
    }

    public UserResponse findBynoHp(String noHp){
        User user = userRepo.findBynoHp(noHp);
        return convertUserToUserResponse(user);
    }
}
