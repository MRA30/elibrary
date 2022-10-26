package com.elibrary.utils;

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
        return Optional.ofNullable(KeycloakAuthenticationToken.class.cast(
                SecurityContextHolder.getContext().getAuthentication()).getAccount()
            .getKeycloakSecurityContext().getToken().getPreferredUsername());
    }
}
