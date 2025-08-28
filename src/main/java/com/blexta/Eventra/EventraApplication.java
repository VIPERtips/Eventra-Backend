package com.blexta.Eventra;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.blexta.Eventra.common.enums.Role;
import com.blexta.Eventra.common.enums.Status;
import com.blexta.Eventra.notification.EmailService;
import com.blexta.Eventra.user.User;
import com.blexta.Eventra.user.UserRepository;

@SpringBootApplication
public class EventraApplication {
	

	public static void main(String[] args) {
		SpringApplication.run(EventraApplication.class, args);
	}
	
	@Bean
    public CommandLineRunner initDefaultAdmins(
            UserRepository userRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {

        return args -> {
            List<User> defaultAdmins = List.of(
                buildUser("viperthehackers@gmail.com", "tadiwa", passwordEncoder),
                buildUser("haroldch2002@gmail.com", "harold", passwordEncoder)
            );

            for (User admin : defaultAdmins) {
                Optional<User> existing = userRepository.findByEmail(admin.getEmail());
                if (existing.isEmpty()) {
                    userRepository.save(admin);
                    emailService.sendAccountActivationEmail(admin.getEmail());
                }
            }
        };
    }

    private User buildUser(String email, String rawPassword, PasswordEncoder passwordEncoder) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();
    }
}