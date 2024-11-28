package com.provoly.event.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.Criticality;

import com.fasterxml.jackson.annotation.JsonCreator;

public final class ExternalEventWriteDto extends EventWriteDto {
    @NotNull
    @NotBlank
    private final String externalSourceRef;
    private final String externalId;

    @JsonCreator
    public ExternalEventWriteDto(Integer id,
            String name,
            String description,
            Criticality criticality,
            String category,
            String subCategory,
            String address,
            UUID equipmentId,
            String domain,
            Instant startDate,
            Instant endDate,
            Instant creationDate,
            Integer parent,
            String externalSourceRef,
            String externalId) {
        super(id, name, description, criticality, category, subCategory, address, equipmentId, domain, startDate, endDate,
                creationDate, parent);
        this.externalId = externalId;
        this.externalSourceRef = externalSourceRef;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }
}
