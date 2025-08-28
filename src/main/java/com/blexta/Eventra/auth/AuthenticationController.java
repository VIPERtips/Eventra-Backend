package com.blexta.Eventra.auth;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.blexta.Eventra.common.dto.LoginDto;
import com.blexta.Eventra.common.dto.SignUpDto;
import com.blexta.Eventra.common.enums.Role;
import com.blexta.Eventra.common.response.AuthenticationResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	@PostMapping("/register")
	@Operation(summary = "Register a new user", description = "Creates a user account with an email, and password. Sends OTP to email for account verification.")
	public ResponseEntity<AuthenticationResponse> registerUser( @Valid
			@RequestBody SignUpDto dto) {
		AuthenticationResponse response = authenticationService.registerUser(dto, Role.USER);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/login")
	@Operation(summary = "User Login", description = "Authenticates the user using email and password. Returns JWT and refresh token if successful.")
	public ResponseEntity<AuthenticationResponse> login(
			@RequestBody LoginDto request) {
		AuthenticationResponse response = authenticationService.authenticate(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/forgot-password")
	@Operation(summary = "Forgot password", description = "Sends a password reset link/token to the provided email.")
	public ResponseEntity<AuthenticationResponse> forgotPassword(@RequestParam String email) {

		authenticationService.forgotPassword(email);
		return ResponseEntity.ok().body(AuthenticationResponse.builder()
				.message("Password reset link sent to your email.")
				.build()
				);

	}

	@PostMapping("/reset-password")
	@Operation(summary = "Reset password", description = "Resets the password using a valid reset token and new password.")
	public ResponseEntity<AuthenticationResponse> resetPassword(@RequestParam String token, @RequestParam String newPassword) {

		authenticationService.resetPassword(token, newPassword);
		return ResponseEntity.ok().body(AuthenticationResponse.builder()
				.message("Password reset successful. You can now log in.")
				.build()
				);

	}

	@PostMapping("/confirm-otp")
	@Operation(summary = "Confirm OTP", description = "Verifies the OTP sent to the user's email and activates the account if the code is valid.")
	public ResponseEntity<AuthenticationResponse> confirmOTP(@RequestParam String email, @RequestParam String otp) {
		authenticationService.confirmOTP(email, otp);
		return ResponseEntity.ok().body(AuthenticationResponse.builder()
				.message("Account verified successfully.")
				.build()
				);
	}

	@PostMapping("/request-otp")
	@Operation(summary = "Request OTP again", description = "Resends a new OTP to the email address if the account is not yet activated.")
	public ResponseEntity<AuthenticationResponse> requestOTP(@RequestParam String email) {
		authenticationService.requestOTP(email);
		return ResponseEntity.ok().body(AuthenticationResponse.builder()
				.message("New OTP sent to your email.")
				.build()
				);
	}

	@PostMapping("/refresh")
	@Operation(summary = "Refresh JWT Token", description = "Generates a new access token using a valid refresh token.")
	public ResponseEntity<AuthenticationResponse> refreshToken(@RequestParam String refreshToken) {
		AuthenticationResponse response = authenticationService.refreshToken(refreshToken);
		return ResponseEntity.ok(response);
	}
}
