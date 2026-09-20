package com.users.mvc.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.users.entity.Credentials;

public interface CredentialsRepository  extends JpaRepository<Credentials, UUID> {

}
