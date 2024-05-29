package com.provoly.service;

import java.time.Instant;
import java.util.UUID;

import com.provoly.equipment.EquipmentShortDto;

public record ServiceReadDto(
        UUID id,
        String externalId,
        String description,
        EquipmentShortDto equipment,
        Instant creationDate,
        Instant lastModificationDate,
        Instant startDate,
        Instant endDate,
        String domain,
        ServiceStatus status,
        String category) {
}
