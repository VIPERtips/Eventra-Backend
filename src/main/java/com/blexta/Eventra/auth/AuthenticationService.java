package com.blexta.Eventra.auth;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.blexta.Eventra.common.dto.LoginDto;
import com.blexta.Eventra.common.dto.SignUpDto;
import com.blexta.Eventra.common.enums.Role;
import com.blexta.Eventra.common.enums.Status;
import com.blexta.Eventra.common.exceptions.ConflictException;
import com.blexta.Eventra.common.exceptions.ResourceNotFoundException;
import com.blexta.Eventra.common.exceptions.UnauthorizedException;
import com.blexta.Eventra.common.response.AuthenticationResponse;
import com.blexta.Eventra.notification.EmailService;
import com.blexta.Eventra.security.JwtService;
import com.blexta.Eventra.user.User;
import com.blexta.Eventra.user.UserRepository;
import com.blexta.Eventra.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final  AuthenticationManager authenticationManager;

	private final UserRepository userRepository;

	private final UserService userService;

	private final EmailService emailService;

	private final PasswordEncoder passwordEncoder;

	private final JwtService jwtService;

	public AuthenticationResponse registerUser(@Valid SignUpDto req, Role role) {
		
	if (userRepository.existsByEmail(req.getEmail())) {
		throw new ConflictException("Oops, email taken. Enter a unique email.");
	}

	User user =User
			.builder()
			.email(req.getEmail())
			.password(passwordEncoder.encode(req.getPassword()))
			.role(role)
			.build();


	String otp = userService.generateOTP();
	user.setOTP(otp);
	user.setOtpExpiration(LocalDateTime.now().plusHours(2));

	userRepository.save(user);
	emailService.sendOtpVerificationEmail(req.getEmail(), otp);

	String token = jwtService.generateToken(user);
	String refreshToken = jwtService.generateRefreshToken(user);

	return AuthenticationResponse.builder()
			.token(token)
			.role(user.getRole().name())
			.refreshToken(refreshToken)
			.message("Registration successful")
			.build();
}

	public void confirmOTP(String email, String otp) {
		User user = userService.getUserByEmail(email);

		if (user.getOTP() == null || !user.getOTP().toString().equals(otp)) {
			throw new ConflictException("Invalid OTP");
		}
		
		if (user.getOtpExpiration() == null || user.getOtpExpiration().isBefore(LocalDateTime.now())) {
		    throw new ConflictException("OTP expired, please request a new one.");
		}

		user.setStatus(Status.ACTIVE);
		user.setOTP(null);
		user.setOtpExpiration(null);
		userRepository.save(user);
		emailService.sendAccountActivationEmail(user.getEmail());
	}

	public void requestOTP(String email) {
		User user = userService.getUserByEmail(email);

		if (user == null) {
			throw new ResourceNotFoundException("User not found");
		}

		if (user.getStatus() == Status.ACTIVE) {
			throw new ConflictException("OTP request is not allowed. Your account is already active.");
		}

		String otp = userService.generateOTP();
		user.setOTP(otp);
		user.setOtpExpiration(LocalDateTime.now().plusHours(2));
		userRepository.save(user);
		emailService.sendOtpVerificationEmail(user.getEmail(),  otp);
	}

	public AuthenticationResponse authenticate(LoginDto req) {
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

		User user = userService.getUserByEmail(req.getEmail());
		if (user.getStatus() != Status.ACTIVE) {
			throw new ConflictException(
					"Please verify your account before you can log in. Check your email for OTP and follow instructions or request new OTP");
		}

		String token = jwtService.generateToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);

		return AuthenticationResponse.builder()
				.token(token)
				.role(user.getRole().name())
				.status(user.getStatus().name())
				.id(user.getUserId())
				.message("Login successful!")
				.refreshToken(refreshToken)
				.build();
		
	}

	public AuthenticationResponse refreshToken(String refreshToken) {
		String email = jwtService.extractUsername(refreshToken);
		User user = userService.getUserByEmail(email);

		if (!jwtService.isValid(refreshToken, user)) {
			throw new UnauthorizedException("Invalid refresh token");
		}

		String newAccessToken = jwtService.generateToken(user);
		return  AuthenticationResponse.builder()
				.token(newAccessToken)
				.role(user.getRole().name())
				.status(user.getStatus().name())
				.id(user.getUserId())
				.refreshToken(refreshToken)
				.build();
		
	}
	
	public void forgotPassword(String email) {
	    User user = userService.getUserByEmail(email);
	    String resetToken = userService.generateResetToken(user);
	    emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
	}

	public void resetPassword(String token, String newPassword) {
	    User user = userService.findByResetToken(token);
	    if (user == null) {
	        throw new ConflictException("Invalid reset token.");
	    }

	    if (user.getTokenExpiration() == null || user.getTokenExpiration().isBefore(LocalDateTime.now())) {
	        throw new ConflictException("Reset token expired.");
	    }

	    userService.updatePassword(user, newPassword);
	}
}

