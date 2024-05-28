package com.provoly.event.dto;

import java.time.Instant;
import java.util.UUID;

import com.provoly.event.Category;
import com.provoly.event.Criticality;
import com.provoly.event.EventType;

public class OperatorEventWriteDto extends EventWriteDto {
    private Instant startDate;
    private Instant endDate;

    public OperatorEventWriteDto(UUID id, String name, String description, Criticality criticality, String address,
            UUID equipmentId, Category category, Instant startDate, Instant endDate, String domain) {
        super(id, name, description, criticality, category, address, equipmentId, EventType.OPERATOR, domain);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }
}
