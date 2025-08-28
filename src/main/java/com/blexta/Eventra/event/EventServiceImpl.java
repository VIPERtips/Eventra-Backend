package com.blexta.Eventra.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.blexta.Eventra.common.dto.EventDto;
import com.blexta.Eventra.common.dto.EventRequest;
import com.blexta.Eventra.common.enums.Category;
import com.blexta.Eventra.common.exceptions.ResourceNotFoundException;
import com.blexta.Eventra.user.User;
import com.blexta.Eventra.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
	private final EventRepository eventRepository;
	private final UserService userService;
	
	@Override
	public EventDto createEvent(EventRequest dto, User user) {
		eventRepository.findByTitleAndLocation(dto.getTitle(), dto.getLocation())
        .ifPresent(e -> {
            throw new IllegalArgumentException("An event with this title and location already exists");
        });
		User creator = userService.getUserByEmail(user.getEmail());
		Event event = Event.builder()
	            .title(dto.getTitle())
	            .description(dto.getDescription())
	            .location(dto.getLocation())
	            .date(dto.getDate())
	            .category(dto.getCategory())
	            .createdBy(creator) 
	            .build();
	    eventRepository.save(event);
	    return mapToDto(event);
	}
	@Override
	public EventDto updateEvent(long id, EventRequest dto,User user) {
		eventRepository.findByTitleAndLocation(dto.getTitle(), dto.getLocation())
        .filter(e -> e.getEventId() != id) 
        .ifPresent(e -> {
            throw new IllegalArgumentException("Another event with this title and location already exists");
        });
		Event event = fetchEventEntityById(id);
		event.setTitle(dto.getTitle());
	    event.setDescription(dto.getDescription());
	    event.setLocation(dto.getLocation());
	    event.setDate(dto.getDate());
	    event.setCategory(dto.getCategory());
	    eventRepository.save(event);
	    return mapToDto(event);
	}
	
	@Override
	public void deleteEvent(long id) {
		if(!eventRepository.existsById(id)) {
			throw new ResourceNotFoundException("Event not found for id: "+id);
		}
		eventRepository.deleteById(id);
		
	}
	
	@Override
	public EventDto getEventById(long id) {
		Event event = fetchEventEntityById(id);
	    return mapToDto(event);
	}
	
	@Override
	public Page<EventDto> getAllEvents(int page, int size) {
		Sort sort = Sort.by(Sort.Direction.DESC,"createdAt");
		Pageable pageable = PageRequest.of(page, size,sort);
		Page<Event> events = eventRepository.findAll(pageable);
		return events.map(this::mapToDto);
	}
	
	@Override
	public Page<EventDto> getEventsByCategory(int page, int size,Category category) {
		Sort sort = Sort.by(Sort.Direction.DESC,"createdAt");
		Pageable pageable = PageRequest.of(page, size,sort);
		Page<Event> events = eventRepository.findByCategory(category,pageable);
		return events.map(this::mapToDto);
	}
	
	private Event fetchEventEntityById(long id) {
	    return eventRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Event not found for id: "+id));
	}
	
	private EventDto mapToDto(Event event) {
	    String creator = event.getCreatedBy() != null ? event.getCreatedBy().getUsername() : "Unknown";
	    return EventDto.builder()
	            .eventId(event.getEventId())
	            .title(event.getTitle())
	            .description(event.getDescription())
	            .location(event.getLocation())
	            .date(event.getDate())
	            .category(event.getCategory())
	            .createdBy(creator)
	            .createdAt(event.getCreatedAt())
	            .build();
	}

}
