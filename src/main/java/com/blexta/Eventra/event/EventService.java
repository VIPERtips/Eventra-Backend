package com.blexta.Eventra.event;

import org.springframework.data.domain.Page;

import com.blexta.Eventra.common.dto.EventRequest;
import com.blexta.Eventra.common.dto.EventDto;
import com.blexta.Eventra.common.enums.Category;
import com.blexta.Eventra.user.User;

public interface EventService {
	EventDto createEvent(EventRequest dto,User user);
	EventDto updateEvent(long id, EventRequest dto,User user);
	void deleteEvent(long id);
	EventDto getEventById(long id);
	Page<EventDto> getAllEvents(int page, int size);
	Page<EventDto> getEventsByCategory(int page, int size,Category category);
	
}
