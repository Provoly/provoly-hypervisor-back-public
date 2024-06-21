package com.provoly.model;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.action.ActionWriteDto;

public record ProcedureModelWriteDto(
        Integer id,
        @NotNull @NotBlank String name,
        @NotNull String description,
        @NotNull String domain,
        @NotNull String creator,
        Collection<ActionWriteDto> actions) {

    public ProcedureModelWriteDto {
        if (actions == null) {
            actions = new ArrayList<>();
        }
    }
}
