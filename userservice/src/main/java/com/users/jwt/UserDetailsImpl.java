package com.users.jwt;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.users.entity.Credentials;
import com.users.entity.Role;
import com.users.entity.User;
import com.users.entity.UserStatus;

import lombok.Getter;

@Getter
public class UserDetailsImpl implements UserDetails {

	private static final long serialVersionUID = 1L;

	private final User user;
	private final Credentials credentials;

	public UserDetailsImpl(User user) {
		this.user = user;
		this.credentials = user.getCredentials();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Role role = credentials.getRole();

		if (role == null) {
			return Collections.emptyList();
		}

		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return credentials.getPassword();
	}

	@Override
	public String getUsername() {
		return credentials.getUsername();
	}

	public String getName() {

		String firstName = user.getFirstName() != null ? user.getFirstName() : "";

		String lastName = user.getLastName() != null ? user.getLastName() : "";

		return (firstName + " " + lastName).trim();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return credentials.getStatus() != UserStatus.BLOCKED;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return credentials.getStatus() == UserStatus.ACTIVE && !user.isDeleted();
	}
}