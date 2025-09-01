package com.blexta.Eventra.event;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blexta.Eventra.common.dto.EventDto;
import com.blexta.Eventra.common.enums.Category;
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
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "Endpoints for retrieving and filtering events")
public class EventController {

    private final EventService eventService;
    private final JwtService jwtService;
    private final UserService userService;

    @Operation(
        summary = "Get all events",
        description = "Fetch a paginated list of all events. This endpoint returns every event, "
                    + "regardless of user registration. Default page is 0 and size is 10.",
        tags = {"Event Management"}
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventDto>>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<EventDto> events = eventService.getAllEvents(page, size);
        return ResponseEntity.ok(
                ApiResponse.<Page<EventDto>>builder()
                        .message("Events fetched successfully")
                        .data(events)
                        .isValid(true)
                        .build()
        );
    }

    @Operation(
        summary = "Get available events for current user",
        description = "Fetch a paginated list of events that the authenticated user can register for. "
                    + "Events where the user is already registered will be excluded. "
                    + "Default page is 0 and size is 10.",
        tags = {"Event Management"}
    )
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Page<EventDto>>> getAvailableEvents(
            HttpServletRequest req,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String token = TokenUtils.extractToken(req);
        Page<EventDto> events;
        if(token != null) {
            String email = jwtService.extractUsername(token);
            User user = userService.getUserByEmail(email);
            events = eventService.getAllEventsForUser(user, page, size);
        } else {
            events = eventService.getAllEvents(page, size);
        }

        return ResponseEntity.ok(
                ApiResponse.<Page<EventDto>>builder()
                        .message("Events fetched successfully")
                        .data(events)
                        .isValid(true)
                        .build()
        );
    }

    @Operation(
        summary = "Get events by category",
        description = "Fetch a paginated list of events filtered by a specific category. "
                    + "Default page is 0 and size is 10.",
        tags = {"Event Management"}
    )
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<EventDto>>> getEventsByCategory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable Category category
    ) {
        Page<EventDto> events = eventService.getEventsByCategory(page, size, category);
        return ResponseEntity.ok(
                ApiResponse.<Page<EventDto>>builder()
                        .message("Events fetched successfully")
                        .data(events)
                        .isValid(true)
                        .build()
        );
    }
}
