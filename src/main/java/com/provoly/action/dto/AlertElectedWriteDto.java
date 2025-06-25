package com.provoly.action.dto;

import com.provoly.event.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AlertElectedWriteDto extends ActionWriteDto {
    @NotNull
    @NotBlank
    private final String name;
    private final String serviceExternalId;

    public AlertElectedWriteDto(UUID id, String type, Status status, String name, String serviceExternalId) {
        super(id, type, status);
        this.name = name;
        this.serviceExternalId = serviceExternalId;
    }

    public String getName() {
        return name;
    }

    public String getServiceExternalId() {
        return serviceExternalId;
    }
}