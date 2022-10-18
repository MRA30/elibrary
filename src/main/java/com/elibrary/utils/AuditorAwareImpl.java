package com.elibrary.utils;

import com.elibrary.config.KeycloakSecurityConfig;
import com.elibrary.dto.response.UserResponse;
import com.elibrary.model.entity.User;
import com.elibrary.services.UserService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Autowired
    private UserService userService;

    @Override
    public Optional<String> getCurrentAuditor() {
        User currentUser = userService.findByEmail(((KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getAccount().getKeycloakSecurityContext().getToken().getEmail());
        if(currentUser != null) {
            return Optional.of(currentUser.getEmail());
        }else {
            return Optional.of("system");
        }
    }
}
