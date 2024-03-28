package com.provoly.event.dto;

import java.time.Instant;

import com.provoly.event.OperatorCategory;

public class OperatorEventReadDto extends EventReadDto {
    private OperatorCategory category;
    private Instant startDate;
    private Instant endDate;

    public OperatorEventReadDto(EventReadDto dto, OperatorCategory category, Instant startDate, Instant endDate) {
        super(dto);
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
