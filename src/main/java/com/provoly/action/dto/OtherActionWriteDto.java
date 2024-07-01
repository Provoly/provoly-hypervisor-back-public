package com.provoly.action.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.Status;

public class OtherActionWriteDto extends ActionWriteDto {
    @NotNull
    @NotBlank
    private final String name;

    public OtherActionWriteDto(UUID id, String type, Status status, String name) {
        super(id, type, status);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
