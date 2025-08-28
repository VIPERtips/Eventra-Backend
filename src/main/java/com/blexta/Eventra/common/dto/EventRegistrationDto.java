package com.blexta.Eventra.common.dto;

import java.time.LocalDateTime;

import com.blexta.Eventra.common.enums.RegistrationStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventRegistrationDto {
    private long eventRegistrationId;
    private String email;
    private long eventId;
    private String eventTitle;
    private RegistrationStatus status;
    private LocalDateTime registeredAt;
}
