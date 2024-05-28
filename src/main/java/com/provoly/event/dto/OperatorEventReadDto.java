package com.provoly.event.dto;

import java.time.Instant;

public class OperatorEventReadDto extends EventReadDto {
    private Instant startDate;
    private Instant endDate;

    public OperatorEventReadDto(EventReadDto dto, Instant startDate, Instant endDate) {
        super(dto);
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
