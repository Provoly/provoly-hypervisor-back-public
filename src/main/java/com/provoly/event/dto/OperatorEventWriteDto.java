package com.provoly.event.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import com.provoly.event.Criticality;
import com.provoly.event.EventType;
import com.provoly.event.OperatorCategory;

public class OperatorEventWriteDto extends EventWriteDto {
    @NotNull
    private OperatorCategory category;
    private Instant startDate;
    private Instant endDate;

    public OperatorEventWriteDto(UUID id, String name, String description, Criticality criticality, String address,
            UUID equipmentId, OperatorCategory category, Instant startDate, Instant endDate, String domain) {
        super(id, name, description, criticality, address, equipmentId, EventType.OPERATOR, domain);
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public OperatorCategory getCategory() {
        return category;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }
}
