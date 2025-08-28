package com.blexta.Eventra.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public class TokenUtils {

	public static String extractToken(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		return (token != null && token.startsWith("Bearer ")) ? token.substring(7) : null;
	}
}
