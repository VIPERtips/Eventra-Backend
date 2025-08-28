package com.blexta.Eventra.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blexta.Eventra.common.dto.EventDto;
import com.blexta.Eventra.common.dto.EventRequest;
import com.blexta.Eventra.common.response.ApiResponse;
import com.blexta.Eventra.common.utils.TokenUtils;
import com.blexta.Eventra.event.EventService;
import com.blexta.Eventra.security.JwtService;
import com.blexta.Eventra.user.User;
import com.blexta.Eventra.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Event Management", description = "Admin endpoints for managing events")
public class AdminEventController {
	private final EventService eventService;
	private final JwtService jwtService;
	private final UserService userService;
	
	@Operation(
        summary = "Create a new event",
        description = "Allows an admin to create a new event. JWT token from the request header is used to identify the admin.",
        tags = {"Admin Event Management"}
    )
	@PostMapping
	public ResponseEntity<ApiResponse<EventDto>> createEvent(
        @RequestBody EventRequest dto,
        HttpServletRequest req
    ){
		String email = jwtService.extractUsername(TokenUtils.extractToken(req));
		User admin = userService.getUserByEmail(email);
		EventDto created = eventService.createEvent(dto, admin);
		return ResponseEntity.ok(
            ApiResponse.<EventDto>builder()
				.message("Event created successfully")
				.data(created)
				.isValid(true)
				.build()
        );
	}
	
	@Operation(
        summary = "Update an existing event",
        description = "Allows an admin to update an existing event by ID. JWT token is used to validate the admin.",
        tags = {"Admin Event Management"}
    )
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<EventDto>> updateEvent(
        @PathVariable long id,
        @RequestBody EventRequest dto,
        HttpServletRequest req
    ){
		String email = jwtService.extractUsername(TokenUtils.extractToken(req));
		User admin = userService.getUserByEmail(email);
		EventDto updated = eventService.updateEvent(id, dto, admin);
		return ResponseEntity.ok(
            ApiResponse.<EventDto>builder()
				.message("Event updated successfully")
				.data(updated)
				.isValid(true)
				.build()
        );
	}
	
	@Operation(
        summary = "Delete an event",
        description = "Allows an admin to delete an event by its ID.",
        tags = {"Admin Event Management"}
    )
	@DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
				.message("Event deleted successfully")
				.isValid(true)
				.build()
        );
    }
	
	
}
