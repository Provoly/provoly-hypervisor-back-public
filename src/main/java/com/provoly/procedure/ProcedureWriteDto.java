package com.provoly.procedure;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.action.dto.ActionWriteDto;
import com.provoly.event.dto.EventWriteDto;

public record ProcedureWriteDto(
        @NotNull Integer id,
        @NotNull @NotBlank String name,
        @NotNull @NotBlank String description,
        @Valid List<EventWriteDto> events,
        @Valid List<ActionWriteDto> actions) {
}
