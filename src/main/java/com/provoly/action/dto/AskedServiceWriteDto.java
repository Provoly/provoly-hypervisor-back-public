package com.provoly.action.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.Status;

public class AskedServiceWriteDto extends ActionWriteDto {
    @NotNull
    @NotBlank
    private final String name;
    private final String serviceExternalId;

    public AskedServiceWriteDto(UUID id, String type, Status status, String name, String serviceExternalId) {
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
