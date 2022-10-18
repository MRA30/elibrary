package com.elibrary.services;

import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.Exception.UserException;
import com.elibrary.config.KeycloakConfig;
import com.elibrary.dto.request.*;
import com.elibrary.dto.response.UserResponse;
import com.elibrary.model.entity.User;
import com.elibrary.model.repos.ImageRepo;
import com.elibrary.model.repos.UserRepo;
import com.mashape.unirest.http.JsonNode;
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
import javax.ws.rs.core.Response;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    @Autowired
    private EmailService emailService;

    @Autowired
    private ImageRepo imageRepo;

    // function for generate random number
    public String generateRandomNumber(int length) {
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < length; i++) {
            number.append((int) (Math.random() * 10));
        }
        return number.toString();
    }

    public List<String> findAllImage(String type, long id){
        return imageRepo.findAllImageByTypeAndId(type, id);
    }

    public User save(User user){
        return userRepo.save(user);
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

      public String getIdKeycloak(Principal principal){
          return ((KeycloakAuthenticationToken) principal)
                  .getAccount().getKeycloakSecurityContext()
                  .getToken().getSubject();
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

    public boolean existsByNumberIdentity(String numberIdentity){
        return userRepo.existsByNumberIdentity("EM" + numberIdentity);
    }

    public UserResponse findBynoHp(String noHp){
        User user = userRepo.findBynoHp(noHp);
        return convertUserToUserResponse(user);
    }

    public boolean existsByUsernameAndPassword(String username, String password){
        Optional<User> user = userRepo.findByUsername(username);
        return user.filter(value -> bCryptPasswordEncoder.matches(password, value.getPassword())).isPresent();
    }

    public User findByEmailVerificationToken(String token){
        return userRepo.findByEmailVerificationToken(token);
    }

    public User findByResetPasswordToken(String token){
        return userRepo.findByPasswordResetToken(token);
    }

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
            user.getUserRole(),
            findAllImage("user", user.getId())
//            user.getBorrows(),
//            user.getBookRequests()
        );
    }

    public User findById(long id){
        Optional<User> user = userRepo.findById(id);
        return user.orElse(null);
    }

    public UserResponse findByIdUserResponse(long id) throws BusinessNotFound {
        Optional<User> user = userRepo.findById(id);
        if(user.isPresent()){
            return convertUserToUserResponse(user.get());
        }
        else {
            throw new BusinessNotFound("User not found");
        }
    }

    public User findByEmail(String email){
        return userRepo.findByEmail(email);
    }

    public List<User> findUserEnabled(){
        return userRepo.findAllEnabled();
    }

    public UserResponse registerEmployee(RegisterEmployeeRequest request) throws UserException {
        if(existsByNumberIdentity(request.getNumberIdentity())){
            throw new UserException("Number Identity already exists");
        }
        if(existByUsername(request.getUsername())){
            throw new UserException("Username already exists");
        }
        if(existsBynoHp(request.getNoHp())){
            throw new UserException("No Hp already exists");
        }
        if(existsByEmail(request.getEmail())){
            throw new UserException("Email already exists");
        }
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
        userKeycloak.setClientRoles(Collections.singletonMap("library", Collections.singletonList("employee")));
        userKeycloak.setEnabled(false);
        userKeycloak.setEmailVerified(false);
        String encodePassword = bCryptPasswordEncoder.encode(request.getPassword());
        request.setPassword(encodePassword);
        User user = new User(
                numberIdentity,
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                false,
                request.getGender(),
                request.getNoHp(),
                request.getAddress(),
                request.getEmail(),
                request.getPassword(),
                userKeycloak.getClientRoles().values().toString().replace("[", "").replace("]", "")
        );

        var response = usersResource.create(userKeycloak);

        if(response.getStatus() == 201){
//            String fullName = user.getFirstName() + " " + user.getLastName();
//            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
//            String link = "http://localhost:8080/activation/" + uuid;
//            user.setEmailVerificationToken(uuid);
//            user.setEmailVerificationTokenExpiry(java.sql.Date.valueOf(format.format(new Date().getTime() + 1000 * 60 * 60)));
//            emailService.sendEmail(request.getEmail(), "Register Employee", "Hello " + fullName + ", your account has been created, please verify your account by clicking the link below, link will expire in 1 hour. " + link);
            save(user);
        }
        return convertUserToUserResponse(user);
    }

    public UserResponse registerMember(RegisterMemberRequest request) throws UserException {
        if (existByUsername(request.getUsername())) {
            throw new UserException("Username already exists");
        }
        if (existsBynoHp(request.getNoHp())) {
            throw new UserException("No Hp already exists");
        }
        if (existsByEmail(request.getEmail())) {
            throw new UserException("Email already exists");
        }
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
        userKeycloak.setEnabled(false);
        userKeycloak.setEmailVerified(true);

        String encodePassword = bCryptPasswordEncoder.encode(request.getPassword());
        request.setPassword(encodePassword);
        User user = new User(
                numberIdentity,
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                false,
                request.getGender(),
                request.getNoHp(),
                request.getAddress(),
                request.getEmail(),
                request.getPassword(),
                userKeycloak.getClientRoles().values().toString().replace("[", "").replace("]", "")
        );

        Response response = keycloakConfig.createUser(userKeycloak);
        System.out.println(response.getStatus());
         if(response.getStatus() == 201){
//             String fullName = user.getFirstName() + " " + user.getLastName();
//             String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
//             String link = "http://localhost:8081/public/activation/" + uuid;
//             user.setEmailVerificationToken(uuid);
////             user.setEmailVerificationTokenExpiry(java.sql.Date.valueOf(format.format(new Date().getTime() + 1000 * 60 * 60)));
//             emailService.sendEmail(request.getEmail(), "Register Employee", "Hello " + fullName + ", your account has been created, please verify your account by clicking the link below, link will expire in 1 hour. " + link);
             save(user);
         }
        return convertUserToUserResponse(user);
    }

    public UserResponse updateUser(String idKeycloak, long id, UpdateProfileRequest request) throws UnirestException, UserException {
        long idEmail = findByEmail(request.getEmail()).getId();
        long idUsername = findByUsername(request.getUsername()).getId();
        long idNoHp = findBynoHp(request.getNoHp()).getId();
        if(existsByEmail(request.getEmail()) && idEmail != id){
            throw new UserException("Email already exists");
        }
        if(existByUsername(request.getUsername()) && idUsername != id){
            throw new UserException("Username already exists");
        }
        if(existsBynoHp(request.getNoHp()) && idNoHp != id){
            throw new UserException("No Hp already exists");
        }
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(idKeycloak);
        userRepresentation.setUsername(request.getUsername());
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
        return convertUserToUserResponse(save(user));
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

    public UserResponse forgotPassword(ResetPasswordEmailRequest request) throws UserException {
        User user = findByEmail(request.getEmail());
        if(user == null){
            throw new UserException("Email not found");
        }
        // generated UUID 30 character
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
        user.setPasswordResetToken(uuid);
        user.setEmailVerificationTokenExpiry(java.sql.Date.valueOf(format.format(new Date().getTime() + 1000 * 60 * 60)));

        return convertUserToUserResponse(save(user));
    }

    public UserResponse resetPassword(ResetPasswordRequest request, String token) throws UserException {
        User user = findByResetPasswordToken(token);
        if(user == null){
            throw new UserException("Token not valid");
        }
        if(user.getEmailVerificationTokenExpiry().before(new Date())){
            throw new UserException("Token expired");
        }
        String encodePassword = bCryptPasswordEncoder.encode(request.getPassword());
        user.setPassword(encodePassword);
        user.setPasswordResetToken(null);
        user.setEmailVerificationTokenExpiry(null);
        return convertUserToUserResponse(save(user));
    }

    public void verifyEmail(String token) throws UserException, UnirestException {
        User user = findByEmailVerificationToken(token);
        if(user == null){
            throw new UserException("Token not valid");
        }
//        if(user.getEmailVerificationTokenExpiry().before(new Date())){
//            throw new UserException("Token expired");
//        }
        JsonNode jsonNode =  keycloakConfig.getKeycloakUserByEmail(user.getEmail());
        String id = jsonNode.getArray().getJSONObject(0).getString("id");
        keycloakConfig.setEnabled(id);
        user.setEnabled(true);
        convertUserToUserResponse(save(user));
    }
}
