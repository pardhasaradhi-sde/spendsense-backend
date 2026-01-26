package com.spendsense.repository;

import com.spendsense.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    //returns a user if present based on clerkuserid
    Optional<User> findByClerkUserId(String clerkUserId);

    //returns a user if present based on email
    Optional<User> findByEmail(String email);

    //checks if a user is present based on clerkuserid
    boolean existsByClerkUserId(String clerkUserId);

    //checks if a user is present based on email
    boolean existsByEmail(String email);

}
