package com.provoly.event.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.Category;
import com.provoly.event.Criticality;
import com.provoly.event.EventType;

public class AlertEventWriteDto extends EventWriteDto {
    @NotNull
    @NotBlank
    private String externalSourceRef;

    public AlertEventWriteDto(UUID id, String name, String description, Criticality criticality, String address,
            UUID equipmentId, Category category, String externalSourceRef, String domain) {
        super(id, name, description, criticality, category, address, equipmentId, EventType.ALERT, domain);
        this.externalSourceRef = externalSourceRef;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }
}
