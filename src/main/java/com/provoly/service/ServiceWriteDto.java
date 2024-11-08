package com.provoly.service;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record ServiceWriteDto(
        @NotNull String id,
        String additionalInfo,
        String description,
        String equipment,
        @NotNull Instant creationDate,
        @NotNull Instant lastModificationDate,
        Instant startDate,
        Instant endDate,
        Instant closeDate,
        String domain,
        @NotNull ServiceStatus status,
        @NotNull String category) {
}
