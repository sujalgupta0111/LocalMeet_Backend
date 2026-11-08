package com.users.service.implementation;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import com.users.entities.LmuOtpAuthentication;
import com.users.repository.LmuOtpRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.users.config.security.jwt.UserDetailsImpl;
import com.users.dtos.mapper.UserMapper;
import com.users.dtos.request.UserDTO;
import com.users.entities.User;
import com.users.enums.RegistrationSource;
import com.users.enums.Role;
import com.users.enums.UserStatus;
import com.users.repository.CredentialsRepository;
import com.users.repository.UserRepository;
import com.users.service.AuthService;

import lombok.RequiredArgsConstructor;

import static org.hibernate.annotations.UuidGenerator.Style.RANDOM;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final CredentialsRepository credentialsRepository;
	private final LmuOtpRepository lmuOtpRepository;

	@Override
	public UserDetailsImpl login(String identifier, String password) {
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(identifier, password));
		return (UserDetailsImpl) authentication.getPrincipal();

	}

	@Override
	@Transactional
	public UserDetailsImpl register(UserDTO request,String action) {
		
		try {
			if("update".equalsIgnoreCase(action)){
				String email =  request.credentials().email();
				User user = userRepository.findActiveUserByIdentifier(email, UserStatus.ACTIVE).orElseThrow(() -> new RuntimeException("User not found"));
				user=userMapper.partialUpdate(request,user);
				user.getCredentials().setPassword(passwordEncoder.encode(request.credentials().password()));
				return new UserDetailsImpl(userRepository.save(user));
			}
			User user = userMapper.toEntity(request);
			user.getCredentials().setPassword(passwordEncoder.encode(request.credentials().password()));
			user.getCredentials().setRole(Role.USER);
			user.getCredentials().setStatus(UserStatus.ACTIVE);
			user.setRegistrationSource(RegistrationSource.LOCAL);
			return new UserDetailsImpl(userRepository.save(user));

		} catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("Username or email already exists");
		}
	}

	@Transactional
	public UserDetailsImpl registerOuth(User user) {

		try {
			String email = user.getCredentials().getEmail();
			Optional<User> existingUser = userRepository.findByCredentialsEmail(email);
			if (existingUser.isPresent()) {
				return new UserDetailsImpl(existingUser.get());
			}
			// New OAuth user
			user.getCredentials().setRole(Role.USER);
			user.getCredentials().setStatus(UserStatus.ACTIVE);
			User savedUser = userRepository.save(user);
			return new UserDetailsImpl(savedUser);

		} catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("Username or email already exists");
		}
	}

	@Override
	public String processOtp(String action, String email, String otp) {
		if (action == null || email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("Action aur Email required fields hain.");
		}

		return switch (action.trim().toUpperCase()) {
			case "GENERATE" -> handleGenerateOtp(email.trim().toLowerCase());
			case "VERIFY" -> handleVerifyOtp(email.trim().toLowerCase(), otp);
			default -> throw new IllegalArgumentException("Invalid action: " + action + ". Only 'GENERATE' or 'VERIFY' allowed.");
		};
	}

	private String handleGenerateOtp(String email) {
		// Purane unverified OTPs delete karo
		lmuOtpRepository.deleteByEmailAndVerifiedFalse(email);
		// 6-digit Secure OTP generate karo
		String generatedOtp = String.valueOf(100000 + new SecureRandom().nextInt(900000));
		// DB me store karne ke liye entity prepare karo
		LmuOtpAuthentication otpAuth = LmuOtpAuthentication.builder()
				.email(email)
				.otp(generatedOtp)
				.verified(false)
				.createdAt(LocalDateTime.now())
				.expiryTime(LocalDateTime.now().plusMinutes(10))
				.build();
		lmuOtpRepository.save(otpAuth);
		// Yahan email trigger karne ka service call aayega: emailService.sendOtp(email, generatedOtp);
		return "OTP Send Successfully";
	}

	private String handleVerifyOtp(String email, String inputOtp) {
		if (inputOtp == null || inputOtp.trim().isEmpty()) {
			throw new IllegalArgumentException("Verification ke liye OTP required hai.");
		}
		// Latest unverified OTP fetch karo
		LmuOtpAuthentication otpRecord = lmuOtpRepository.findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(email)
				                         .orElseThrow(() -> new RuntimeException("OTP record not found for email: " + email));
		// Expiry check karo
		if (otpRecord.getExpiryTime() != null && otpRecord.getExpiryTime().isBefore(LocalDateTime.now())) {
			return "OTP expired";
		}
		// OTP matching
		if (!otpRecord.getOtp().equals(inputOtp.trim())) {
			return "Incorrect OTP. ";
		}

		// OTP verify mark karo taaki dobara use na ho sake
		otpRecord.setVerified(true);
		lmuOtpRepository.save(otpRecord);

		return "OTP Verification Successful.";
	}


}