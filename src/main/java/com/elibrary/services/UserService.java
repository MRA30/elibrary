package com.elibrary.services;

import com.elibrary.config.KeycloakConfig;
import com.elibrary.dto.request.RegisterEmployeeRequest;
import com.elibrary.dto.request.RegisterMemberRequest;
import com.elibrary.dto.request.UpdatePasswordRequest;
import com.elibrary.dto.request.UpdateProfileRequest;
import com.elibrary.dto.response.UserResponse;
import com.elibrary.model.entity.User;
import com.elibrary.model.repos.UserRepo;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Value("${keycloak.realm}")
    public String realm;

    @Value("${keycloak.resource}")
    public String clientId;

    @Autowired
    private KeycloakConfig keycloakConfig;

    @Autowired
    private UserRepo userRepo;

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

    public boolean existsById(long id) {
        return userRepo.existsById(id);
    }

    public UserResponse getProfile(Principal principal){
         String username = ((KeycloakAuthenticationToken) principal)
                .getAccount().getKeycloakSecurityContext()
                .getToken().getPreferredUsername();
        return findByUsername(username);
      }

      public UserResponse findByUsername(String username){
        Optional<User> user = userRepo.findByUsername(username);
          return user.map(this::convertUserToUserResponse).orElse(null);
      }

    public boolean existsByEmail(String email){
        return userRepo.existsByEmail(email);
    }

    public boolean existsBynoHp(String noHp){
        return userRepo.existsBynoHp(noHp);
    }

    public boolean existByUsername(String username) { return userRepo.existsByUsername(username);}

    public UserResponse findBynoHp(String noHp){
        User user = userRepo.findBynoHp(noHp);
        return convertUserToUserResponse(user);
    }

    public boolean existsByUsernameAndPassword(String username, String password){
        Optional<User> user = userRepo.findByUsername(username);
        if(user.isPresent()){
            return bCryptPasswordEncoder.matches(password, user.get().getPassword());
        }
        return false;
    }

    public List<String> existsByEmailUsernameNoHpWithOtherUsername(UpdateProfileRequest updateProfileRequest, long id){
        List<String> messagesList = new ArrayList<>();
        long idEmail = findByEmail(updateProfileRequest.getEmail()).getId();
        long idUsername = findByUsername(updateProfileRequest.getUsername()).getId();
        long idNoHp = findBynoHp(updateProfileRequest.getNoHp()).getId();
        if(existsByEmail(updateProfileRequest.getEmail()) && idEmail != id){
            messagesList.add("Email already exists");
        }
        if(existByUsername(updateProfileRequest.getUsername()) && idUsername != id){
            messagesList.add("Username already exists");
        }
        if(existsBynoHp(updateProfileRequest.getNoHp()) && idNoHp != id){
            messagesList.add("No Hp already exists");
        }
        return messagesList;
    }

//    public User getUser(){
//        return userRepo.findByUsername(principal().getName());
//    }
//
//    public Long getId(){
//        return userRepo.findByUsername(principal().getName()).getId();
//    }

    public UserResponse convertUserToUserResponse(User user){
        return new UserResponse(
            user.getId(),
            user.getNumberIdentity(),
            user.getUsername(),
            user.getFirstName() + " " + user.getLastName(),
            user.getGender(),
            user.getNoHp(),
            user.getAddress(),
            user.getEmail(),
            user.getUserRole()
//            user.getBorrows(),
//            user.getBookRequests()
        );
    }

    public User findById(long id){
        Optional<User> user = userRepo.findById(id);
        return user.orElse(null);
    }

    public UserResponse findByIdUserResponse(long id){
        Optional<User> user = userRepo.findById(id);
        return user.map(this::convertUserToUserResponse).orElse(null);
    }

    public UserResponse findByEmail(String email){
        User user = userRepo.findByEmail(email);
        if(user != null){
            return convertUserToUserResponse(user);
        }
        return null;
    }

    public UserResponse registerEmployee(RegisterEmployeeRequest request){
        UsersResource usersResource = keycloakConfig.getInstance().realm(realm).users();
        String emailLowerCase = request.getEmail().toLowerCase();
        request.setEmail(emailLowerCase);

        String numberIdentity = "EM" + request.getNumberIdentity();
        request.setNumberIdentity(numberIdentity);
        UserRepresentation userKeycloak = new UserRepresentation();
        userKeycloak.setUsername(request.getUsername());
        userKeycloak.setFirstName(request.getFirstName());
        userKeycloak.setLastName(request.getLastName());
        userKeycloak.setEmail(request.getEmail());
        userKeycloak.setCredentials(Collections.singletonList(createPasswordCredentials(request.getPassword())));
        userKeycloak.setClientRoles(Collections.singletonMap(realm, Collections.singletonList("employee")));
        userKeycloak.setEnabled(true);
        String encodePassword = bCryptPasswordEncoder.encode(request.getPassword());
        request.setPassword(encodePassword);
        User user = new User(
                numberIdentity,
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                true,
                request.getGender(),
                request.getNoHp(),
                request.getAddress(),
                request.getEmail(),
                request.getPassword(),
                userKeycloak.getClientRoles().values().toString().replace("[", "").replace("]", "")
        );

        var response = usersResource.create(userKeycloak);

        if(response.getStatus() == 201){
            userRepo.save(user);
        }
        return convertUserToUserResponse(user);
    }

    public UserResponse registerMember(RegisterMemberRequest request) {
        UsersResource usersResource = keycloakConfig.getInstance().realm(realm).users();
        String emailLowerCase = request.getEmail().toLowerCase();
        request.setEmail(emailLowerCase);

        String generateRandomNumber = generateRandomNumber(3);
        String numberIdentity = "ME" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + generateRandomNumber;

        UserRepresentation userKeycloak = new UserRepresentation();
        userKeycloak.setUsername(request.getUsername());
        userKeycloak.setFirstName(request.getFirstName());
        userKeycloak.setLastName(request.getLastName());
        userKeycloak.setEmail(request.getEmail());
        userKeycloak.setCredentials(Collections.singletonList(createPasswordCredentials(request.getPassword())));
        userKeycloak.setClientRoles(Collections.singletonMap(clientId, Collections.singletonList("member")));
        userKeycloak.setEnabled(true);
        userKeycloak.setEmailVerified(true);

        String encodePassword = bCryptPasswordEncoder.encode(request.getPassword());
        request.setPassword(encodePassword);
        User user = new User(
                numberIdentity,
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                true,
                request.getGender(),
                request.getNoHp(),
                request.getAddress(),
                request.getEmail(),
                request.getPassword(),
                userKeycloak.getClientRoles().values().toString().replace("[", "").replace("]", "")
        );

        var response = usersResource.create(userKeycloak);
         if(response.getStatus() == 201){
             userRepo.save(user);
         }
        return convertUserToUserResponse(user);
    }

    public UserResponse updateUser(long id, UpdateProfileRequest request) throws UnirestException {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(request.getFirstName());
        userRepresentation.setLastName(request.getLastName());
        userRepresentation.setEmail(request.getEmail());
        var response =  keycloakConfig.updateKeycloakUser(userRepresentation);
        String emailLowerCase = request.getEmail().toLowerCase();
        request.setEmail(emailLowerCase);
        User user  = findById(id);
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setGender(request.getGender());
        user.setNoHp(request.getNoHp());
        user.setAddress(request.getAddress());
        user.setEmail(request.getEmail());
        System.out.println(response);
        userRepo.save(user);
        return convertUserToUserResponse(user);
    }

    public UserResponse updatePassword(long id, UpdatePasswordRequest request){
            User user = findById(id);
            String encodePassword = bCryptPasswordEncoder.encode(request.getNewPassword());
            user.setPassword(encodePassword);
            userRepo.save(user);
            return convertUserToUserResponse(user);
    }

    public Page<UserResponse> findAllUsers(String search, String userRole, int page, int size, String sortBy, String direction){
        Pageable pageable;
        if(direction.equals("desc")){
            pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        } else{
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        }
        Page<User> employees = userRepo.findAllUsers(search, userRole, pageable);
        int totalElement = employees.getNumberOfElements();
        return new PageImpl<>(employees.getContent()
                .stream().map(this::convertUserToUserResponse)
                .collect(Collectors.toList()), pageable, totalElement);
    }

    public List<UserResponse> findAllWithoutPaging(String search){
        List<User> members = userRepo.findAllWithoutPaging(search);
        return members.stream().map(this::convertUserToUserResponse).collect(Collectors.toList());
    }

    private static CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);
        return passwordCredentials;
    }
}
