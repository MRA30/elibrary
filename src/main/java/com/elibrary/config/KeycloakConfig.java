package com.elibrary.config;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.Getter;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.core.Response;

@Configuration
@Getter
public class KeycloakConfig {
    private static Keycloak keycloak = null;
    @Value("${keycloak.auth-server-url}")
    public String serverUrl;

    @Value("${keycloak.realm}")
    public String realm;

    @Value("${keycloak.resource}")
    public String clientId;

    @Value("${keycloak.credentials.secret}")
    public String clientSecret;

    private final String clientKeycloakId = "39550ad5-fd4f-44ee-949e-4b7e5582c690";

    public KeycloakConfig() {
    }

    public Keycloak getInstance(){
        if(keycloak == null){

            keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.PASSWORD)
                    .username("munifatirra")
                    .password("root")
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .resteasyClient(
                            new ResteasyClientBuilder()
                                    .connectionPoolSize(10).build())
                    .build();
        }
        return keycloak;
    }

    public Response createUser(UserRepresentation userRepresentation) {
        return keycloak.realm(realm).users().create(userRepresentation);
    }

    public KeycloakBuilder newKeycloakBuilderWithPasswordCredentials(String username, String password) {
        return KeycloakBuilder.builder()
                .realm(realm)
                .serverUrl(serverUrl)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password);
    }

    public JsonNode updateKeycloakUser(UserRepresentation userRepresentation) throws UnirestException {
        String url = serverUrl + "/admin/realms/" + realm + "/users/" + userRepresentation.getId();
        return Unirest.put(url)
                .header("Authorization", "Bearer " + getInstance().tokenManager().getAccessTokenString())
                .header("Content-Type", "application/json")
                .body("{\n" +
//                        "    \"username\":username \"" + userRepresentation.getUsername() + "\",\n" +
                        "    \"firstName\": \"" + userRepresentation.getFirstName() + "\",\n" +
                        "    \"lastName\": \"" + userRepresentation.getLastName() + "\",\n" +
                        "    \"email\": \"" + userRepresentation.getEmail() + "\"\n" +
                        "}")
                .asJson().getBody();
    }

    // check username and password exists in keycloak
    public boolean checkUser(String username, String password) {
        try {
            Keycloak keycloak = newKeycloakBuilderWithPasswordCredentials(username, password).build();
            keycloak.tokenManager().getAccessToken();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setEnabled(String id, String isEnabled) throws UnirestException {
        String url = serverUrl + "/admin/realms/" + realm + "/users/" + id;
        Unirest.put(url)
                .header("Authorization", "Bearer " + getInstance().tokenManager().getAccessTokenString())
                .header("Content-Type", "application/json")
                .body("{\n" +
                        "   \"enabled\":" + isEnabled + "\n" +
                        "}")
                .asJson();
    }

    public void setEnabledAndVerified(UserRepresentation user) {
        user.setEnabled(true);
        user.setEmailVerified(true);
        keycloak.realm(realm).users().get(user.getId()).update(user);
    }

    public JsonNode getKeycloakUserByEmail(String email) throws UnirestException {
        String url = serverUrl + "/admin/realms/" + realm + "/users?email=" + email;
        return Unirest.get(url)
                .header("Authorization", "Bearer " + getInstance().tokenManager().getAccessTokenString())
                .header("Content-Type", "application/json")
                .asJson().getBody();
    }

    public JsonNode getKeycloakUserByUsername(String username) throws UnirestException {
        String url = serverUrl + "/admin/realms/" + realm + "/users?username=" + username;
        return Unirest.get(url)
            .header("Authorization", "Bearer " + getInstance().tokenManager().getAccessTokenString())
            .header("Content-Type", "application/json")
            .asJson().getBody();
    }

    // change password
    public void changePassword(String id, String password) throws UnirestException {
        String url = serverUrl + "/admin/realms/" + realm + "/users/" + id + "/reset-password";
        Unirest.put(url)
            .header("Authorization",
                "Bearer " + getInstance().tokenManager().getAccessTokenString())
            .header("Content-Type", "application/json")
            .body("{\n" +
                "    \"type\": \"password\",\n" +
                "    \"temporary\": false,\n" +
                "    \"value\": \"" + password + "\"\n" +
                "}")
            .asJson().getBody();
    }

    // get role list
    public JsonNode getRoleList() throws UnirestException {
        String url = serverUrl + "/admin/realms/" + realm + "/clients/" + clientKeycloakId + "/roles";
        return Unirest.get(url)
                .header("Authorization", "Bearer " + getInstance().tokenManager().getAccessTokenString())
                .header("Content-Type", "application/json")
                .asJson().getBody();
    }
    public void changeUserRole(String userId, String roleName) throws UnirestException {
        JsonNode roleList = getRoleList();

        String roleId = "";
        for (int i = 0; i < roleList.getArray().length(); i++) {
            if (roleList.getArray().getJSONObject(i).get("name").toString().equals(roleName)) {
                roleId = roleList.getArray().getJSONObject(i).get("id").toString();
            }
        }

        // find client role
        String url = serverUrl + "/admin/realms/" + realm + "/users/" +
            userId + "/role-mappings/clients/" + clientKeycloakId;
        Unirest.post(url)
            .header("Authorization", "Bearer " + getInstance().tokenManager().getAccessTokenString())
            .header("Content-Type", "application/json")
            .body("[{\"id\": \"" + roleId + "\","
                + "\"name\": \"" + roleName + "\", "
                + "\"scopeParamRequired\": false, "
                + "\"composite\": false,"
                + " \"clientRole\": true, "
                + "\"containerId\": \"ec9cff5f-22a8-4664-83b9-ee126911871b\"}]")
            .asJson()
            .getBody();
    }

    // delete session
    public void deleteSession(String userId) throws UnirestException {
        String url = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/logout";
        Unirest.post(url)
            .header("Authorization", "Bearer " + getInstance().tokenManager().getAccessTokenString())
            .header("Content-Type", "application/json")
            .asJson()
            .getBody();
    }

    public JsonNode getAllUsers() throws UnirestException {
        String url = serverUrl + "/admin/realms/" + realm + "/users";
        return Unirest.get(url)
                .header("Authorization", "Bearer " + getInstance().tokenManager().getAccessTokenString())
                .header("Content-Type", "application/json")
                .asJson().getBody();
    }

    public JsonNode refreshToken(String refreshToken) throws UnirestException {
        String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        return Unirest.post(url)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("client_id", clientId)
                .field("client_secret", clientSecret)
                .field("refresh_token", refreshToken)
                .field("grant_type", "refresh_token")
                .asJson().getBody();
    }

}
