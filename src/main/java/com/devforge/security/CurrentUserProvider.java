package com.devforge.security;

import com.devforge.security.principal.UserPrincipal;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public UserPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user found");
        }
        return principal;
    }

    public Long requireUserId() {
        return requirePrincipal().getId();
    }

    public String requireEmail() {
        return requirePrincipal().getEmail();
    }
}
