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
import java.time.Instant;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserService {

  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Value("${keycloak.realm}")
  public String realm;

  @Value("${keycloak.resource}")
  public String clientId;

  private final KeycloakConfig keycloakConfig;

  private final UserRepo userRepo;

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  private final EmailService emailService;

  private final ImageRepo imageRepo;

  // function for generate random number
  public String generateRandomNumber(int length) {
    StringBuilder number = new StringBuilder();
    for (int i = 0; i < length; i++) {
      number.append((int) (Math.random() * 10));
    }
    return number.toString();
  }

  public List<String> findAllImage(String type, long id) {
    return imageRepo.findAllImageByTypeAndId(type, id);
  }

  public User save(User user) {
    return userRepo.save(user);
  }

  public boolean existsById(long id) {
    return userRepo.existsById(id);
  }

  public UserResponse getProfile(Principal principal) {
    String username = ((KeycloakAuthenticationToken) principal)
        .getAccount().getKeycloakSecurityContext()
        .getToken().getPreferredUsername();
    return convertUserToUserResponse(findByUsername(username));
  }

  public String getIdKeycloak(Principal principal) {
    return ((KeycloakAuthenticationToken) principal)
        .getAccount().getKeycloakSecurityContext()
        .getToken().getSubject();
  }

  public User findByUsername(String username) {
    Optional<User> user = userRepo.findByUsername(username);
    return user.orElse(null);
  }

  public boolean existsByEmail(String email) {
    return userRepo.existsByEmail(email);
  }

  public boolean existsBynoHp(String noHp) {
    return userRepo.existsBynoHp(noHp);
  }

  public boolean existByUsername(String username) {
    return userRepo.existsByUsername(username);
  }

  public boolean existsByNumberIdentity(String numberIdentity) {
    return userRepo.existsByNumberIdentity("EM" + numberIdentity);
  }

  public User findByNoHp(String noHp) {
    Optional<User> user = userRepo.findByNoHp(noHp);
    return user.orElse(null);
  }

  public User findByEmailVerificationToken(String token) {
    return userRepo.findByEmailVerificationToken(token);
  }

  public User findByResetPasswordToken(String token) {
    return userRepo.findByPasswordResetToken(token);
  }

  public boolean isEnabled(String username) throws UnirestException {
    return keycloakConfig.getKeycloakUserByUsername(username).getArray().getJSONObject(0)
        .getBoolean("enabled");
  }

  public boolean isVerified(String username) throws UnirestException {
    return keycloakConfig.getKeycloakUserByUsername(username).getArray().getJSONObject(0)
        .getBoolean("emailVerified");
  }

  public UserResponse convertUserToUserResponse(User user) {

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

  public User findById(long id) {
    Optional<User> user = userRepo.findById(id);
    return user.orElse(null);
  }

  public UserResponse findByIdUserResponse(long id) throws BusinessNotFound {
    Optional<User> user = userRepo.findById(id);
    if (user.isPresent()) {
      return convertUserToUserResponse(user.get());
    } else {
      throw new BusinessNotFound("User not found");
    }
  }

  public User findByEmail(String email) {
    return userRepo.findByEmail(email);
  }

  public List<User> findUserEnabled() {
    return userRepo.findAllEnabled();
  }

  public UserResponse registerEmployee(RegisterEmployeeRequest request)
      throws UserException, UnirestException {
    if (existsByNumberIdentity(request.getNumberIdentity())) {
      throw new UserException("Number Identity already exists");
    }
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

    String numberIdentity = "EM" + request.getNumberIdentity();
    request.setNumberIdentity(numberIdentity);
    UserRepresentation userKeycloak = new UserRepresentation();
    userKeycloak.setUsername(request.getUsername());
    userKeycloak.setFirstName(request.getFirstName());
    userKeycloak.setLastName(request.getLastName());
    userKeycloak.setEmail(request.getEmail());
    userKeycloak.setCredentials(
        Collections.singletonList(createPasswordCredentials(request.getPassword())));
    userKeycloak.setClientRoles(
        Collections.singletonMap("library", Collections.singletonList("employee")));
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

    if (response.getStatus() == 201) {
      String fullName = user.getFirstName() + " " + user.getLastName();
      String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
      String link = "http://localhost:8081/public/activation/" + uuid;
      user.setEmailVerificationToken(uuid);
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.HOUR, 1);
      user.setEmailVerificationTokenExpiry(calendar.getTime());
      emailService.sendEmail(request.getEmail(), "Register Employee", "Hello " + fullName
          + ", your account has been created, please verify your account by clicking the link below, link will expire in 1 hour. "
          + link);
      save(user);
      var jsonNode = keycloakConfig.getKeycloakUserByEmail(request.getEmail());
      String id = jsonNode.getArray().getJSONObject(0).getString("id");
      keycloakConfig.changeUserRole(id, "employee");
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
    String numberIdentity =
        "ME" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + generateRandomNumber;

    UserRepresentation userKeycloak = new UserRepresentation();
    userKeycloak.setUsername(request.getUsername());
    userKeycloak.setFirstName(request.getFirstName());
    userKeycloak.setLastName(request.getLastName());
    userKeycloak.setEmail(request.getEmail());
    userKeycloak.setCredentials(
        Collections.singletonList(createPasswordCredentials(request.getPassword())));
    userKeycloak.setClientRoles(
        Collections.singletonMap(clientId, Collections.singletonList("member")));
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

    Response response = keycloakConfig.createUser(userKeycloak);
    System.out.println(response.getStatus());
    if (response.getStatus() == 201) {
      String fullName = user.getFirstName() + " " + user.getLastName();
      String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
      String link = "http://localhost:8081/public/activation/" + uuid;
      user.setEmailVerificationToken(uuid);
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.HOUR, 1);
      user.setEmailVerificationTokenExpiry(calendar.getTime());
      emailService.sendEmail(request.getEmail(), "Register Member", "Hello " + fullName
          + ", your account has been created, please verify your account by clicking the link below, link will expire in 1 hour. "
          + link);
      save(user);
    }
    return convertUserToUserResponse(user);
  }

  public UserResponse updateUser(String idKeycloak, long id, UpdateProfileRequest request)
      throws UnirestException, UserException {
    User userEmail = findByEmail(request.getEmail());
    User userUsername = findByUsername(request.getUsername());
    User userNoHp = findByNoHp(request.getNoHp());
    if (existsByEmail(request.getEmail())) {
      if (userEmail.getId() != id) {
        throw new UserException("Email already exists");
      }
    }
    if (existByUsername(request.getUsername())) {
      if (userUsername.getId() != id) {
        throw new UserException("Username already exists");
      }
    }
    if (existsBynoHp(request.getNoHp())) {
      if (userNoHp.getId() != id) {
        throw new UserException("No Hp already exists");
      }
    }
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentation.setId(idKeycloak);
    userRepresentation.setUsername(request.getUsername());
    userRepresentation.setFirstName(request.getFirstName());
    userRepresentation.setLastName(request.getLastName());
    userRepresentation.setEmail(request.getEmail());
    var response = keycloakConfig.updateKeycloakUser(userRepresentation);
    String emailLowerCase = request.getEmail().toLowerCase();
    request.setEmail(emailLowerCase);
    User user = findById(id);
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

  public void updatePassword(ChangePasswordRequest request, Principal principal)
      throws UnirestException, UserException {
    User user = findByEmail(getProfile(principal).getEmail());
    if (!bCryptPasswordEncoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new UserException("Old password is wrong");
    }
    if (bCryptPasswordEncoder.matches(request.getNewPassword(), user.getPassword())) {
      throw new UserException("New password cannot be the same as old password");
    }
    String encodePassword = bCryptPasswordEncoder.encode(request.getNewPassword());
    user.setPassword(encodePassword);
    userRepo.save(user);
    JsonNode jsonNode = keycloakConfig.getKeycloakUserByEmail(user.getEmail());
    String idUser = jsonNode.getArray().getJSONObject(0).getString("id");
    keycloakConfig.changePassword(idUser, request.getNewPassword());
  }

  public Page<UserResponse> findAllUsers(String search, String userRole, int page, int size,
      String sortBy, String direction) {
    Pageable pageable;
    if (direction.equals("desc")) {
      pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
    } else {
      pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
    }
    Page<User> employees = userRepo.findAllUsers(search, userRole, pageable);
    int totalElement = employees.getNumberOfElements();
    return new PageImpl<>(employees.getContent()
        .stream().map(this::convertUserToUserResponse)
        .collect(Collectors.toList()), pageable, totalElement);
  }

  public List<UserResponse> findAllWithoutPaging(String search) {
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

  public void forgotPassword(EmailRequest request) throws UserException {
    User user = findByEmail(request.getEmail());
    if (user == null) {
      throw new UserException("Email not found");
    }
    // generated UUID 30 character
    String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
    user.setPasswordResetToken(uuid);
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.HOUR, 1);
    user.setPasswordResetTokenExpiry(calendar.getTime());
  }

  public void resetPassword(ResetPasswordRequest request, String token)
      throws UserException, UnirestException {
    User user = findByResetPasswordToken(token);
    if (user == null) {
      throw new UserException("Token not valid");
    }
    if (Date.from(Instant.now()).after(user.getPasswordResetTokenExpiry())) {
      throw new UserException("Token expired");
    }
    String encodePassword = bCryptPasswordEncoder.encode(request.getPassword());
    user.setPassword(encodePassword);
    JsonNode jsonNode = keycloakConfig.getKeycloakUserByEmail(user.getEmail());
    String id = jsonNode.getArray().getJSONObject(0).getString("id");
    keycloakConfig.changePassword(id, request.getPassword());
    user.setPasswordResetToken(null);
    user.setPasswordResetTokenExpiry(null);
  }

  public void verifyEmail(String token) throws UserException, UnirestException {
    User user = findByEmailVerificationToken(token);
    if (user == null) {
      throw new UserException("Token not valid");
    }
    if (Date.from(Instant.now()).after(user.getEmailVerificationTokenExpiry())) {
      throw new UserException("Token expired");
    }
    JsonNode jsonNode = keycloakConfig.getKeycloakUserByEmail(user.getEmail());
    String id = jsonNode.getArray().getJSONObject(0).getString("id");
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentation.setId(id);
    userRepresentation.setEnabled(true);
    userRepresentation.setEmailVerified(true);
    keycloakConfig.setEnabledAndVerified(userRepresentation);
    user.setEnabled(true);
    user.setEmailVerificationToken(null);
    user.setEmailVerificationTokenExpiry(null);
    convertUserToUserResponse(save(user));
  }

  public void resendVerificationToken(EmailRequest request) throws UserException {
    User user = findByEmail(request.getEmail());
    if (user == null) {
      throw new UserException("Email not found");
    }
    if (user.isEnabled()) {
      throw new UserException("Email already verified");
    }
    if (user.getEmailVerificationToken() == null) {
      throw new UserException("You can't resend verification token");
    }
    String fullName = user.getFirstName() + " " + user.getLastName();
    String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
    String link = "http://localhost:8081/public/activation/" + uuid;
    user.setEmailVerificationToken(uuid);
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.HOUR, 1);
    user.setEmailVerificationTokenExpiry(calendar.getTime());
    emailService.sendEmail(user.getEmail(), "Register Employee", "Hello " + fullName
        + ", your account has been created, please verify your account by clicking the link below, link will expire in 1 hour. "
        + link);
  }
}
