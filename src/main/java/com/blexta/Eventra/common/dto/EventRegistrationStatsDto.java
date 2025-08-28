package com.blexta.Eventra.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventRegistrationStatsDto {
    private long eventId;
    private String eventTitle;
    private long totalRegistrations;
    private long registeredCount;
    private long cancelledCount;
}
