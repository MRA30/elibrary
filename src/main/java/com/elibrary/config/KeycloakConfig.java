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
                        "    \"firstName\": \"" + userRepresentation.getFirstName() + "\",\n" +
                        "    \"lastName\": \"" + userRepresentation.getLastName() + "\",\n" +
                        "    \"email\": \"" + userRepresentation.getEmail() + "\"\n" +
                        "}")
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
