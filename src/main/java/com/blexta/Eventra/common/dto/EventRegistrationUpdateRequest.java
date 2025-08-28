package com.blexta.Eventra.common.dto;

import com.blexta.Eventra.common.enums.RegistrationStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventRegistrationUpdateRequest {
    private RegistrationStatus status;
}
