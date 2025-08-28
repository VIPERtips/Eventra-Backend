package com.blexta.Eventra.security;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	private static final Logger logger = LoggerFactory.getLogger(Logger.class);

	@Value("${jwt_secret_key}")
	private String SECRET_KEY;

	private final long EXPIRATION_TIME = 86400000L;
	private final long REFRESH_EXPIRATION_TIME = 604800000L;
	
	public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        boolean valid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        logger.info("Token validation status: {} for user: {}", valid, username);
        return valid;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claim = extractClaims(token);
        return resolver.apply(claim);
    }

	
	
	private Claims extractClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignKey())
				.build().parseClaimsJws(token).getBody();
	}
	
	public String generateToken(UserDetails userDetails) {
		String token = Jwts.builder()
				.setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(getSignKey())
				.compact();
				logger.info("Generated JWT Token for user: {}", userDetails.getUsername());
        return token;
				
	}
	
	 public String generateRefreshToken(UserDetails userDetails) {
	        String refreshToken = Jwts.builder()
	                .setSubject(userDetails.getUsername())
	                .setIssuedAt(new Date(System.currentTimeMillis()))
	                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
	                .signWith(getSignKey())
	                .compact();
	        logger.info("Generated Refresh Token for user: {}", userDetails.getUsername());
	        return refreshToken;
	    }


	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
