package com.provoly.event.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.Criticality;
import com.provoly.event.EventType;
import com.provoly.event.ReportCategory;

public class ReportEventWriteDto extends EventWriteDto {
    @NotNull
    private ReportCategory category;
    @NotNull
    @NotBlank
    private String externalSourceRef;

    public ReportEventWriteDto(UUID id, String name, String description, Criticality criticality, String address,
            UUID equipmentId, ReportCategory category, String externalSourceRef, String domain) {
        super(id, name, description, criticality, address, equipmentId, EventType.REPORT, domain);
        this.category = category;
        this.externalSourceRef = externalSourceRef;
    }

    public ReportCategory getCategory() {
        return category;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }
}
