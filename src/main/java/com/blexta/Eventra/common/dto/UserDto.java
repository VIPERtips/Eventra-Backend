package com.blexta.Eventra.common.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
	private long id;
	private String email;
	private LocalDateTime createdAt;
}
