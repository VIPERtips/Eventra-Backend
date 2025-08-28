package com.blexta.Eventra.filter;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.blexta.Eventra.security.JwtService;
import com.blexta.Eventra.security.JwtUserDetailsService;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  
  @Autowired
  private JwtService jwtService;
  
  @Autowired
  private JwtUserDetailsService userDetailsImpl;
  
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) 
	        throws ServletException, IOException {

	    String authHeader = request.getHeader("Authorization");

	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        logger.warn("No JWT token found in request headers");
	        filterChain.doFilter(request, response);
	        return;
	    }

	    String token = authHeader.substring(7);

	    try {
	        String username = jwtService.extractUsername(token);
	        logger.info("Extracted username from token: {}", username);

	        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
	            UserDetails userDetails = userDetailsImpl.loadUserByUsername(username);

	            if (jwtService.isValid(token, userDetails)) {
	                logger.info("JWT Token is valid for user: {}", username);
	                UsernamePasswordAuthenticationToken authToken = 
	                    new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
	                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                SecurityContextHolder.getContext().setAuthentication(authToken);
	            } else {
	                logger.error("Invalid JWT token for user: {}", username);
	            }
	        }
	        filterChain.doFilter(request, response);

	    } catch (io.jsonwebtoken.ExpiredJwtException e) {
	        logger.warn("JWT token expired: {}", e.getMessage());
	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	        response.setContentType("application/json");
	        response.getWriter().write("{\"error\":\"Token expired, please login again\"}");
	        response.getWriter().flush();
	    } catch (Exception e) {
	        logger.error("JWT processing failed: {}", e.getMessage());
	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	        response.setContentType("application/json");
	        response.getWriter().write("{\"error\":\"Invalid token\"}");
	        response.getWriter().flush();
	    }
	}

}
