package com.blexta.Eventra.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginDto {
	private String email;
	private String password;
}
