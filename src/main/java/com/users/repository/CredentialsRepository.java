package com.users.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.users.entities.Credentials;



public interface CredentialsRepository  extends JpaRepository<Credentials, UUID> {

}
