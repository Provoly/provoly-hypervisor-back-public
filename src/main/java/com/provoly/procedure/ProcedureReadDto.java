package com.provoly.procedure;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import com.provoly.action.ActionReadDto;
import com.provoly.event.dto.EventReadDto;

public record ProcedureReadDto(
        UUID id,
        String name,
        Instant creationDate,
        Collection<ActionReadDto> actions,
        Collection<EventReadDto> events,
        float progress) {
}
