package com.provoly.procedure;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.dto.EventWriteDto;

public record ProcedureWriteDto(
        @NotNull Integer id,
        @NotNull @NotBlank String name,
        List<EventWriteDto> events) {
}
