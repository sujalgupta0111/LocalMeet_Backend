package com.users.service;

import com.users.config.security.jwt.UserDetailsImpl;
import com.users.dtos.request.UserDTO;
import com.users.entities.User;
import jakarta.transaction.Transactional;


public interface AuthService {

	UserDetailsImpl register(UserDTO user,String action);

	UserDetailsImpl registerOuth(User user);

	UserDetailsImpl login(String username, String password);
	

	@Transactional
	String processOtp(String action, String email, String otp);


}