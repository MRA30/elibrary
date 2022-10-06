package com.elibrary.controllers;

import com.elibrary.Constans;
import com.elibrary.config.KeycloakConfig;
import com.elibrary.dto.request.LoginRequest;
import com.elibrary.dto.request.RegisterEmployeeRequest;
import com.elibrary.dto.request.RegisterMemberRequest;
import com.elibrary.dto.request.UpdateProfileRequest;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.dto.response.UserResponse;
import com.elibrary.services.UserService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    private final KeycloakConfig keycloakConfig;

    public UserController(KeycloakConfig keycloakConfig) {
        this.keycloakConfig = keycloakConfig;
    }

    @PostMapping("/employee/register")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<UserResponse>> registerEmployee(@Valid @RequestBody RegisterEmployeeRequest registerRequest, Errors errors){
        List<String> messagesList = new ArrayList<>();
        try {
            if (errors.hasErrors()) {
                for (ObjectError error : errors.getAllErrors()) {
                    messagesList.add(error.getDefaultMessage());
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            UserResponse userResponse = userService.registerEmployee(registerRequest);
            messagesList.add("Register Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponse));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/employee")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<Page<UserResponse>>> getAllUsers(@RequestParam(defaultValue = "") String search,
                                                                        @RequestParam(defaultValue = "member") String userRole,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        @RequestParam(defaultValue = "id") String sortBy,
                                                                        @RequestParam(defaultValue = "asc") String direction){
        List<String> messagesList = new ArrayList<>();
        try {
            Page<UserResponse> userResponses = userService.findAllUsers(search.toLowerCase(), userRole.toLowerCase(), page, size, sortBy, direction.toLowerCase());
            messagesList.add("Get All Users Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponses));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/employee/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<UserResponse>> getDetailsUser(@PathVariable("id") Long id){
        List<String> messagesList = new ArrayList<>();
        try {
            if (!userService.existsById(id)) {
                messagesList.add("User Not Found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseData<>(false, messagesList, null));
            }
            UserResponse userResponse = userService.findByIdUserResponse(id);
            messagesList.add("Get Details Profile Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponse));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/employee/allmemeberwithoutpaging")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<List<UserResponse>>> getAllMemberWithoutPaging(@RequestParam(defaultValue = "") String search){
        List<String> messagesList = new ArrayList<>();
        try {
            List<UserResponse> userResponses = userService.findAllWithoutPaging(search);
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponses));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @PostMapping("/public/register")
    public ResponseEntity<ResponseData<UserResponse>> registerMember(@Valid @RequestBody RegisterMemberRequest registerRequest, Errors errors){
        List<String> messagesList = new ArrayList<>();
        try {
            if (errors.hasErrors()) {
                for (ObjectError error : errors.getAllErrors()) {
                    messagesList.add(error.getDefaultMessage());
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            UserResponse userResponse = userService.registerMember(registerRequest);
            messagesList.add("Register Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponse));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @PostMapping("/public/login")
    public ResponseEntity<ResponseData<UserResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                             HttpServletResponse response, Errors errors) {
        List<String> messagesList = new ArrayList<>();
        try {
            if (errors.hasErrors()) {
                for (ObjectError error : errors.getAllErrors()) {
                    messagesList.add(error.getDefaultMessage());
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            if (!userService.existsByUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword())) {
                messagesList.add("Username or Password is wrong");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            Keycloak keycloak = keycloakConfig.newKeycloakBuilderWithPasswordCredentials(loginRequest.getUsername(), loginRequest.getPassword()).build();
            AccessTokenResponse accessTokenResponse = keycloak.tokenManager().getAccessToken();
            Cookie newCookie = new Cookie(Constans.ACCESS_TOKEN, accessTokenResponse.getToken());
            newCookie.setHttpOnly(true);
            newCookie.setPath("/");
            newCookie.setSecure(true);
            newCookie.setMaxAge((int) accessTokenResponse.getExpiresIn());
            response.addCookie(newCookie);

            messagesList.add("Login Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/profile")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<UserResponse>> profileUser(Principal principal) {
        List<String> messagesList = new ArrayList<>();
        try {
            UserResponse userResponse = userService.getProfile(principal);
            messagesList.add("Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponse));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @PutMapping("/update")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<UserResponse>> updateUser(Principal principal, @Valid @RequestBody UpdateProfileRequest updateProfileRequest, Errors errors) throws UnirestException {
        UserResponse userResponse = userService.getProfile(principal);
        List<String> messagesList = new ArrayList<>();
        try {
            if (errors.hasErrors()) {
                for (ObjectError error : errors.getAllErrors()) {
                    messagesList.add(error.getDefaultMessage());
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
            }
            List<String> cekValid = userService.existsByEmailUsernameNoHpWithOtherUsername(updateProfileRequest, userResponse.getId());
            if (cekValid.size() > 0) {
                messagesList.addAll(cekValid);
                return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
            }
            UserResponse update = userService.updateUser(userResponse.getId(), updateProfileRequest);
            messagesList.add("User Updated");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, update));
        }catch (Exception e){
            messagesList.add(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @PostMapping("/image")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<UserResponse>> uploadImage(@RequestParam("image") MultipartFile image, Principal principal) throws IOException {
        UserResponse userResponse = userService.getProfile(principal);
        List<String> messagesList = new ArrayList<>();
//        try {
            System.out.println(image.getOriginalFilename());
            UserResponse update = userService.uploadImage(image, userResponse.getId());
            messagesList.add("Image has been uploaded");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, update));
//        }catch (Exception e){
//            messagesList.add(e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
//        }
    }

}
