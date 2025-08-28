package com.blexta.Eventra.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.blexta.Eventra.common.dto.EventRegistrationDto;
import com.blexta.Eventra.common.dto.EventRegistrationStatsDto;
import com.blexta.Eventra.common.enums.RegistrationStatus;
import com.blexta.Eventra.common.response.ApiResponse;
import com.blexta.Eventra.common.utils.TokenUtils;
import com.blexta.Eventra.security.JwtService;
import com.blexta.Eventra.user.User;
import com.blexta.Eventra.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/event-registrations")
@RequiredArgsConstructor
@Tag(name = "Event Registration", description = "Endpoints for managing event registrations")
public class EventRegistrationController {

    private final EventRegistrationService registrationService;
    private final JwtService jwtService;
    private final UserService userService;

    @Operation(summary = "Register current user to an event",
               description = "Registers the authenticated user to the specified event")
    @PostMapping("/{eventId}/register")
    public ResponseEntity<ApiResponse<EventRegistrationDto>> registerToEvent(
            @PathVariable long eventId,HttpServletRequest req) {
    	String email = jwtService.extractUsername(TokenUtils.extractToken(req));
		User attendee = userService.getUserByEmail(email);
        EventRegistrationDto registration = registrationService.registerUserToEvent(eventId, attendee);
        return ResponseEntity.ok(
                ApiResponse.<EventRegistrationDto>builder()
                        .message("Successfully registered for event")
                        .data(registration)
                        .isValid(true)
                        .build()
        );
    }

    @Operation(summary = "Update registration status",
               description = "Updates the status of a specific registration")
    @PutMapping("/{registrationId}/status")
    public ResponseEntity<ApiResponse<EventRegistrationDto>> updateStatus(
            @PathVariable long registrationId,
            @RequestParam RegistrationStatus status,HttpServletRequest req) {
    	String email = jwtService.extractUsername(TokenUtils.extractToken(req));
		User attendee = userService.getUserByEmail(email);
        EventRegistrationDto updated = registrationService.updateRegistrationStatus(registrationId, status,attendee);
        return ResponseEntity.ok(
                ApiResponse.<EventRegistrationDto>builder()
                        .message("Registration status updated")
                        .data(updated)
                        .isValid(true)
                        .build()
        );
    }

    @Operation(summary = "Get all registrations for current user",
               description = "Fetch paginated list of event registrations for the authenticated user")
    @GetMapping("/my-registrations")
    public ResponseEntity<ApiResponse<Page<EventRegistrationDto>>> getMyRegistrations(
            HttpServletRequest req,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
    	String email = jwtService.extractUsername(TokenUtils.extractToken(req));
		User attendee = userService.getUserByEmail(email);
        Page<EventRegistrationDto> registrations = registrationService.getRegistrationsForUser(attendee, page, size);
        return ResponseEntity.ok(
                ApiResponse.<Page<EventRegistrationDto>>builder()
                        .message("User registrations fetched successfully")
                        .data(registrations)
                        .isValid(true)
                        .build()
        );
    }

    @Operation(summary = "Get all registrations for an event",
               description = "Fetch paginated list of registrations for a given event")
    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<Page<EventRegistrationDto>>> getRegistrationsForEvent(
            @PathVariable long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EventRegistrationDto> registrations = registrationService.getRegistrationsForEvent(eventId, page, size);
        return ResponseEntity.ok(
                ApiResponse.<Page<EventRegistrationDto>>builder()
                        .message("Event registrations fetched successfully")
                        .data(registrations)
                        .isValid(true)
                        .build()
        );
    }

    @Operation(summary = "Get registration stats for an event",
               description = "Returns counts of total, registered, and cancelled registrations for a given event")
    @GetMapping("/event/{eventId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventRegistrationStatsDto>> getEventStats(@PathVariable long eventId) {
        EventRegistrationStatsDto stats = registrationService.getEventRegistrationStats(eventId);
        return ResponseEntity.ok(
                ApiResponse.<EventRegistrationStatsDto>builder()
                        .message("Event registration stats fetched successfully")
                        .data(stats)
                        .isValid(true)
                        .build()
        );
    }
}
