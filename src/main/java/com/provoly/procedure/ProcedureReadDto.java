package com.provoly.procedure;

import java.time.Instant;
import java.util.Collection;

import com.provoly.action.dto.ActionReadDto;
import com.provoly.event.dto.EventReadDto;

public record ProcedureReadDto(
        Integer id,
        String name,
        String description,
        Instant creationDate,
        Collection<ActionReadDto> actions,
        Collection<EventReadDto> events,
        float progress,
        String closeComment) {
}
