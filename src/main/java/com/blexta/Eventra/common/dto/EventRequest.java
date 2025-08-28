package com.blexta.Eventra.common.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.blexta.Eventra.common.enums.Category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventRequest {
	private String title;
	private String description;
	private String location;
	private LocalDate date;
	private Category category;
}
