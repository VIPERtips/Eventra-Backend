package com.blexta.Eventra.user;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.blexta.Eventra.common.dto.UserDto;
import com.blexta.Eventra.common.exceptions.ResourceNotFoundException;
import com.blexta.Eventra.notification.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private static final SecureRandom secureRandom = new SecureRandom();

	public String generateOTP() {
	    int otp = 100000 + secureRandom.nextInt(900000);
	    return String.valueOf(otp);
	}

	public User findById(long id) {
		return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found for Id: " + id));
	}

	public String generateResetToken(User user) {
		String token = UUID.randomUUID().toString();
		user.setResetToken(token);
		user.setTokenExpiration(LocalDateTime.now().plusHours(1));
		userRepository.save(user);
		return user.getResetToken();
	}

	public User findByResetToken(String token) {
		return userRepository.findByResetToken(token);
	}

	public void updatePassword(User user, String newPassword) {
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setResetToken(null);
		user.setTokenExpiration(null);
		userRepository.save(user);
		emailService.sendPasswordChangeConfirmation(user.getEmail());

	}

	public UserDto getUserDtoById(long id) {
		User user = findById(id); 
	    return UserDto.builder()
	            .id(user.getUserId())
	            .email(user.getEmail())
	            .build();
	}

	public void deleteUserById(long id) {
		if (!userRepository.existsById(id)) {
			throw new ResourceNotFoundException("User not found with ID: " + id);
		}
		userRepository.deleteById(id);
	}

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found for email " + email));
	}
}
