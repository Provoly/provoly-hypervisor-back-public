package com.provoly.procedure;

import java.time.Instant;
import java.util.Collection;

import com.provoly.action.ActionReadDto;
import com.provoly.event.dto.EventReadDto;

public record ProcedureReadDto(
        Integer id,
        String name,
        Instant creationDate,
        Collection<ActionReadDto> actions,
        Collection<EventReadDto> events,
        float progress) {
}
