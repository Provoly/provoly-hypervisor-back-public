package com.provoly.event.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.AlertCategory;
import com.provoly.event.Criticality;
import com.provoly.event.EventType;

public class AlertEventWriteDto extends EventWriteDto {
    @NotNull
    private AlertCategory category;

    @NotNull
    @NotBlank
    private String externalSourceRef;

    public AlertEventWriteDto(UUID id, String name, String description, Criticality criticality, String address,
            UUID equipmentId, AlertCategory category, String externalSourceRef, String domain) {
        super(id, name, description, criticality, address, equipmentId, EventType.ALERT, domain);
        this.category = category;
        this.externalSourceRef = externalSourceRef;
    }

    public AlertCategory getCategory() {
        return category;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }
}
