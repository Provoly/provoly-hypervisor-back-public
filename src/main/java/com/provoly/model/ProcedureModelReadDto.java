package com.provoly.model;

import java.time.Instant;
import java.util.Collection;

import com.provoly.action.ActionReadDto;

public record ProcedureModelReadDto(
        Integer id,
        String name,
        String description,
        Instant creationDate,
        Instant lastModificationDate,
        String creator,
        String domain,
        int useCount,
        Collection<ActionReadDto> actions) {
}
