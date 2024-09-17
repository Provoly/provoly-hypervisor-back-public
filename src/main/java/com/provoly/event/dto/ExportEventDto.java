package com.provoly.event.dto;

import java.time.Instant;
import java.util.List;

public record ExportEventDto(Integer id,
        String name,
        String address,
        String description,
        String criticality,
        String category,
        String status,
        Instant lastModificationDate,
        Instant creationDate,
        Instant closeDate,
        String equipment,
        List<Integer> linkedEvents,
        float procedureProgress,
        String domain,
        Instant startDate,
        Instant endDate,
        String externalSourceRef,
        List<String> services,
        Integer parent) {
}
