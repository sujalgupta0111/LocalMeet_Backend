package com.users.service;

import com.users.config.security.jwt.UserDetailsImpl;
import com.users.dtos.request.UserDTO;
import com.users.entities.User;


public interface AuthService {

	UserDetailsImpl register(UserDTO user);

	UserDetailsImpl registerOuth(User user);

	UserDetailsImpl login(String username, String password);
	
	void logout(String username);

}