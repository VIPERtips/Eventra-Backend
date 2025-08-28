package com.blexta.Eventra.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.blexta.Eventra.common.enums.Category;
import com.blexta.Eventra.user.User;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long eventId;
	private String title;
	private String description;
	private String location;
	private LocalDate date;
	@Enumerated(EnumType.STRING)
	private Category category;
	@ManyToOne
	@JoinColumn(name = "created_by")
	private User createdBy;
	private LocalDateTime createdAt;
	
	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
