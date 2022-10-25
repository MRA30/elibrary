package com.elibrary.controllers;

import com.elibrary.Constans;
import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.UserException;
import com.elibrary.config.KeycloakConfig;
import com.elibrary.dto.request.ChangePasswordRequest;
import com.elibrary.dto.request.EmailRequest;
import com.elibrary.dto.request.LoginRequest;
import com.elibrary.dto.request.RegisterEmployeeRequest;
import com.elibrary.dto.request.RegisterMemberRequest;
import com.elibrary.dto.request.ResetPasswordRequest;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    private final KeycloakConfig keycloakConfig;

    public UserController(KeycloakConfig keycloakConfig) {
        this.keycloakConfig = keycloakConfig;
    }

    @PostMapping("/register-employee")
//    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<UserResponse>> registerEmployee(@Valid @RequestBody RegisterEmployeeRequest registerRequest)
        throws UserException, UnirestException {
        Map<String, String> messagesList = new HashMap<>();
            UserResponse userResponse = userService.registerEmployee(registerRequest);
            messagesList.put(Constans.MESSAGE, "Register Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponse));
    }

    @GetMapping("/employee")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<Page<UserResponse>>> getAllUsers(@RequestParam(defaultValue = "") String search,
                                                                        @RequestParam(defaultValue = "member") String userRole,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        @RequestParam(defaultValue = "id") String sortBy,
                                                                        @RequestParam(defaultValue = "asc") String direction){
        Map<String, String> messagesList = new HashMap<>();
        try {
            Page<UserResponse> userResponses = userService.findAllUsers(search.toLowerCase(), userRole.toLowerCase(), page, size, sortBy, direction.toLowerCase());
            messagesList.put(Constans.MESSAGE, "Get All Users Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponses));
        }catch (Exception e){
            messagesList.put(Constans.MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @GetMapping("/employee/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<UserResponse>> getDetailsUser(@PathVariable("id") Long id) throws BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
            UserResponse userResponse = userService.findByIdUserResponse(id);
            messagesList.put(Constans.MESSAGE, "Get Details Profile Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponse));
    }

    @GetMapping("/employee/allmemeberwithoutpaging")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<List<UserResponse>>> getAllMemberWithoutPaging(@RequestParam(defaultValue = "") String search){
        Map<String, String> messagesList = new HashMap<>();
            List<UserResponse> userResponses = userService.findAllWithoutPaging(search);
            messagesList.put(Constans.MESSAGE, "Get All Member Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponses));
    }

    @GetMapping("/profile")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<UserResponse>> profileUser(Principal principal) {
        Map<String, String> messagesList = new HashMap<>();
        try {
            UserResponse userResponse = userService.getProfile(principal);
            messagesList.put(Constans.MESSAGE, "Success");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponse));
        }catch (Exception e){
            messagesList.put(Constans.MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseData<>(false, messagesList, null));
        }
    }

    @PutMapping("/update")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<UserResponse>> updateUser(Principal principal, @Valid @RequestBody UpdateProfileRequest updateProfileRequest) throws UnirestException, UserException {
        UserResponse userResponse = userService.getProfile(principal);
        Map<String, String> messagesList = new HashMap<>();
        String idKeycloak = userService.getIdKeycloak(principal);
        UserResponse update = userService.updateUser(idKeycloak,userResponse.getId(), updateProfileRequest);
        messagesList.put(Constans.MESSAGE, "User Updated");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, update));
    }

    @PutMapping("/update/change-password")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<UserResponse>> changePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest request) throws UnirestException, UserException {
        Map<String, String> messagesList = new HashMap<>();
        userService.updatePassword(request, principal);
        messagesList.put(Constans.MESSAGE, "Password Updated");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @PostMapping("/public/register")
    public ResponseEntity<ResponseData<UserResponse>> registerMember(@Valid @RequestBody RegisterMemberRequest registerRequest) throws UserException {
        Map<String, String> messagesList = new HashMap<>();
        UserResponse userResponse = userService.registerMember(registerRequest);
        messagesList.put(Constans.MESSAGE, "Register Success");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, userResponse));
    }

    @PostMapping("/public/login")
    public ResponseEntity<ResponseData<UserResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
        HttpServletResponse response) throws UnirestException {
        Map<String, String> messagesList = new HashMap<>();
        if(!keycloakConfig.checkUser(loginRequest.getUsername(), loginRequest.getPassword())){
            messagesList.put(Constans.MESSAGE, "Username or Password is Wrong");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
        }
        if(!userService.isEnabled(loginRequest.getUsername())){
            messagesList.put(Constans.MESSAGE, "User is disabled");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseData<>(false, messagesList, null));
        }
        if(!userService.isVerified(loginRequest.getUsername())){
            messagesList.put(Constans.MESSAGE, "User is not verified");
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

        messagesList.put(Constans.MESSAGE, "Login Success");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @PostMapping("/public/activation/{token}")
    public ResponseEntity<ResponseData<UserResponse>> verification(@PathVariable("token") String token) throws UserException, UnirestException {
        Map<String, String> messagesList = new HashMap<>();
        userService.verifyEmail(token);
        messagesList.put(Constans.MESSAGE, "Verification Success");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @PostMapping("/public/forgot-password")
    public ResponseEntity<ResponseData<UserResponse>> forgotPassword(@Valid @RequestBody EmailRequest emailRequest) throws UserException, UnirestException {
        Map<String, String> messagesList = new HashMap<>();
        userService.forgotPassword(emailRequest);
        messagesList.put(Constans.MESSAGE, "Forgot Password Success, Please Check Your Email");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @PostMapping("/public/reset-password/{token}")
    public ResponseEntity<ResponseData<UserResponse>> resetPassword(@PathVariable("token") String token, @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) throws UserException, UnirestException {
        Map<String, String> messagesList = new HashMap<>();
        userService.resetPassword(resetPasswordRequest, token);
        messagesList.put(Constans.MESSAGE, "Reset Password Success, Please Login With New Password");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @PostMapping("/public/resend-verification")
    public ResponseEntity<ResponseData<UserResponse>> resendVerification(@Valid @RequestBody EmailRequest request) throws UserException, UnirestException {
        Map<String, String> messagesList = new HashMap<>();
        userService.resendVerificationToken(request);
        messagesList.put(Constans.MESSAGE, "Resend Verification Success, Please Check Your Email");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }


}
