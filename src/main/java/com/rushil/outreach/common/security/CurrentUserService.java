package com.rushil.outreach.common.security;

import com.rushil.outreach.common.exception.ResourceNotFoundException;
import com.rushil.outreach.user.User;
import com.rushil.outreach.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Resolves the authenticated user for the current request.
 *
 * <p>Every service that touches owner-scoped data goes through here rather than
 * accepting an ownerId parameter from a controller. A controller that can pass in
 * an id is a controller that can pass in the wrong id, and that is exactly the
 * shape of a multi-tenant data leak.
 */
@Service
public class CurrentUserService {

    private final UserRepository users;

    public CurrentUserService(UserRepository users) {
        this.users = users;
    }

    public User require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        String email = auth.getName();
        return users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public UUID requireId() {
        return require().getId();
    }
}