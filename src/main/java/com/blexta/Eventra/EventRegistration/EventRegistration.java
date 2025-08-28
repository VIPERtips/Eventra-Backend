package com.blexta.Eventra.EventRegistration;

import java.time.LocalDateTime;

import com.blexta.Eventra.common.enums.RegistrationStatus;
import com.blexta.Eventra.event.Event;
import com.blexta.Eventra.user.User;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class EventRegistration {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long eventRegistrationId;
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	@ManyToOne
	@JoinColumn(name = "event_id")
	private Event event;
	@Enumerated(EnumType.STRING)
	private RegistrationStatus status;
	
	private LocalDateTime registeredAt;
	
	@PrePersist
	public void onRegister() {
		this.status = RegistrationStatus.REGISTERED;
		this.registeredAt = LocalDateTime.now();
	}
}
