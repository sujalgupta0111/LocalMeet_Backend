package com.users.mvc.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.users.entity.User;
import com.users.jwt.UserDetailsImpl;
import com.users.mvc.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
		User user = userRepository.findActiveUserByIdentifier(identifier)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));

		return new UserDetailsImpl(user);
	}
}