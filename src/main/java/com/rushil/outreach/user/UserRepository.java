package com.rushil.outreach.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

// Repository for managing User entities
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find a user by their email address
    Optional<User> findByEmail(String email);

    // Check if a user with the given email already exists
    boolean existsByEmail(String email);
}
