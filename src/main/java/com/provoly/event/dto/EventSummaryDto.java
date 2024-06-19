package com.provoly.event.dto;

import java.time.Instant;
import java.util.Map;

import com.provoly.event.Criticality;
import com.provoly.event.EventType;
import com.provoly.event.Status;

public record EventSummaryDto(Integer id,
        String name,
        Criticality criticality,
        Status status,
        EventType type,
        Instant lastModificationDate,
        String category,
        String serviceTitle,
        Long serviceCount,
        Map<String, Instant> manifestation,
        Integer procedureId) {
}
