
package com.users.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.users.enums.*;
import com.users.entities.User;



import com.users.enums.*;
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
          AND c.status = :status
    """)
    Optional<User> findActiveUserByIdentifier( @Param("identifier") String identifier,@Param("status") UserStatus status);


    /**
     * Finds a user by email.
     */
    Optional<User> findByCredentialsEmail(String email);

}
