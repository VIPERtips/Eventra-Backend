package com.blexta.Eventra.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	User findByResetToken(String token);

	boolean existsByEmail(String email);

}
