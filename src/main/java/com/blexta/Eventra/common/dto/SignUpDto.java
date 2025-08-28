package com.blexta.Eventra.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpDto {
	private String email;
	private String password;
}
