package com.provoly.action;

import java.time.Instant;
import java.util.UUID;

import com.provoly.event.Status;

public record ActionReadDto(
        UUID id,
        String name,
        ActionType type,
        Status status,
        Instant lastModificationDate) {
}
