package com.provoly.action;

import java.util.UUID;

import com.provoly.event.Status;

public record ActionWriteDto(
        UUID id,
        String name,
        String description,
        Status status,
        ActionType type) {
}
