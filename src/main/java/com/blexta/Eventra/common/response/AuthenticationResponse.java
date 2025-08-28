package com.blexta.Eventra.common.response;



import com.blexta.Eventra.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
    private String token;
    private String role;
    private String refreshToken;
    private String message;
    private String status;
    private Long id;
	private User user;
}
