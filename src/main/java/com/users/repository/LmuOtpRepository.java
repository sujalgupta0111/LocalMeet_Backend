package com.users.repository;

import com.users.entities.LmuOtpAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface LmuOtpRepository extends JpaRepository<LmuOtpAuthentication, Long> {

    // Fetch latest unverified OTP by email
    Optional<LmuOtpAuthentication>findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(String email);

    @Modifying
    @Transactional
    // Delete previous unverified OTPs before generating new OTP
    void deleteByEmailAndVerifiedFalse(String email);

}