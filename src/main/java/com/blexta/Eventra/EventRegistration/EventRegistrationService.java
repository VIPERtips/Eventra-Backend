package com.blexta.Eventra.EventRegistration;

import org.springframework.data.domain.Page;

import com.blexta.Eventra.common.dto.EventRegistrationDto;
import com.blexta.Eventra.common.dto.EventRegistrationStatsDto;
import com.blexta.Eventra.common.enums.RegistrationStatus;
import com.blexta.Eventra.user.User;

public interface EventRegistrationService {

	EventRegistrationDto registerUserToEvent(long eventId, User user);

	EventRegistrationDto updateRegistrationStatus(long registrationId, RegistrationStatus status,User user);

    Page<EventRegistrationDto> getRegistrationsForUser(User user, int page, int size);

    Page<EventRegistrationDto> getRegistrationsForEvent(long eventId, int page, int size);

    EventRegistrationStatsDto getEventRegistrationStats(long eventId);

}

