package com.provoly.event.dto;

import java.time.Instant;

import com.provoly.event.Criticality;
import com.provoly.event.Status;

public record EventSummaryDto(Integer id,
        String name,
        Criticality criticality,
        Status status,
        Instant lastModificationDate,
        String category,
        String serviceTitle,
        Long serviceCount,
        Instant startDate,
        Instant endDate,
        Integer procedureId) {
}
