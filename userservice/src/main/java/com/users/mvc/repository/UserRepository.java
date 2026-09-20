
package com.users.mvc.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.users.entity.User;


public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds an active, non-deleted user using either username or email.
     */
    @Query("""
        SELECT u
        FROM User u
        JOIN u.credentials c
        WHERE (c.email = :identifier OR c.username = :identifier)
          AND u.deleted = false
          AND c.status = com.users.entity.UserStatus.ACTIVE
    """)
    Optional<User> findActiveUserByIdentifier( @Param("identifier") String identifier);

    /**
     * Finds a user by username.
     */
    Optional<User> findByCredentialsUsername(String username);

    /**
     * Finds a user by email.
     */
    Optional<User> findByCredentialsEmail(String email);

    /**
     * Checks whether username already exists.
     */
    boolean existsByCredentialsUsername(String username);

    /**
     * Checks whether email already exists.
     */
    boolean existsByCredentialsEmail(String email);

    /**
     * Finds a non-deleted user by username.
     */
    Optional<User> findByCredentialsUsernameAndDeletedFalse(String username);

    /**
     * Finds a non-deleted user by email.
     */
    Optional<User> findByCredentialsEmailAndDeletedFalse(String email);
}
