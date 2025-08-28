package com.blexta.Eventra.EventRegistration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.blexta.Eventra.common.dto.EventRegistrationDto;
import com.blexta.Eventra.common.dto.EventRegistrationStatsDto;
import com.blexta.Eventra.common.enums.RegistrationStatus;
import com.blexta.Eventra.common.exceptions.ConflictException;
import com.blexta.Eventra.common.exceptions.ResourceNotFoundException;
import com.blexta.Eventra.event.Event;
import com.blexta.Eventra.event.EventRepository;
import com.blexta.Eventra.user.User;
import com.blexta.Eventra.user.UserService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class EventRegistrationImpl implements EventRegistrationService {

    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserService userService;
    private final EventRepository eventRepository;

    @Override
    public EventRegistrationDto registerUserToEvent(long eventId, User user) {
        User attendee = userService.getUserByEmail(user.getEmail());
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if(eventRegistrationRepository.existsByUserAndEvent(attendee, event)) {
            throw new ConflictException("User already registered for this event");
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(attendee)
                .build();

        EventRegistration saved = eventRegistrationRepository.save(registration);

        return mapToDto(saved);
    }

    @Override
    public EventRegistrationDto updateRegistrationStatus(long registrationId, RegistrationStatus status,User user) {
    	User attendee = userService.getUserByEmail(user.getEmail());
    	
        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + registrationId));
        
        if ((registration.getUser().getUserId() != attendee.getUserId())) {
            throw new ConflictException("You are not allowed to update this registration");
        }


        registration.setStatus(status);
        EventRegistration updated = eventRegistrationRepository.save(registration);
        return mapToDto(updated);
    }


    @Override
    public Page<EventRegistrationDto> getRegistrationsForUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registeredAt"));
        Page<EventRegistration> registrations = eventRegistrationRepository.findByUser(user, pageable);
        return registrations.map(this::mapToDto);
    }

    @Override
    public Page<EventRegistrationDto> getRegistrationsForEvent(long eventId, int page, int size) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registeredAt"));
        Page<EventRegistration> registrations = eventRegistrationRepository.findByEvent(event, pageable);
        return registrations.map(this::mapToDto);
    }

    @Override
    public EventRegistrationStatsDto getEventRegistrationStats(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

       

        long total = eventRegistrationRepository.countByEvent(event);
        long registered = eventRegistrationRepository.countByEventAndStatus(event, RegistrationStatus.REGISTERED);
        long cancelled = eventRegistrationRepository.countByEventAndStatus(event, RegistrationStatus.CANCELLED);


        return EventRegistrationStatsDto.builder()
                .eventId(event.getEventId())
                .eventTitle(event.getTitle())
                .totalRegistrations(total)
                .registeredCount(registered)
                .cancelledCount(cancelled)
                .build();
    }

    private EventRegistrationDto mapToDto(EventRegistration registration) {
        return EventRegistrationDto.builder()
                .eventRegistrationId(registration.getEventRegistrationId())
                .eventId(registration.getEvent().getEventId())
                .eventTitle(registration.getEvent().getTitle())
                .email(registration.getUser().getEmail())
                .status(registration.getStatus())
                .registeredAt(registration.getRegisteredAt())
                .build();
    }
}
