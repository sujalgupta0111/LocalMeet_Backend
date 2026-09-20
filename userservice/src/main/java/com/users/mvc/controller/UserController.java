package com.users.mvc.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.users.dtos.ApiResponse;
import com.users.dtos.UserDTO;
import com.users.mvc.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
	
	final UserService userService;
	@GetMapping(value = "/usesa")
	public String hello() {
		return "Hello from UserController";
	}

	@PostMapping(value = "/register")
	public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody UserDTO user) {
//		UserDTO savedUserDto /= userService.addUser(user);

		

		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}





	
	

}
