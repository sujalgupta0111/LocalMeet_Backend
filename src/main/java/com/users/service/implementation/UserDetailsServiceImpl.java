package com.users.service.implementation;

import com.users.enums.UserStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.users.config.security.jwt.UserDetailsImpl;
import com.users.entities.User;
import com.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
		User user = userRepository.findActiveUserByIdentifier(identifier, UserStatus.ACTIVE)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));

		return new UserDetailsImpl(user);
	}
}