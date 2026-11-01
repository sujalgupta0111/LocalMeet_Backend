package com.users.service.implementation;

import java.util.Optional;

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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final CredentialsRepository credentialsRepository;

	@Override
	public UserDetailsImpl login(String identifier, String password) {

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(identifier, password));
		return (UserDetailsImpl) authentication.getPrincipal();

	}

	@Override
	@Transactional
	public UserDetailsImpl register(UserDTO request) {
		
		try {
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
	public void logout(String username) {
		// TODO Auto-generated method stub
		
	}



}