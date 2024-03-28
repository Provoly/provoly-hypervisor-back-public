package com.provoly.event;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("OPERATOR")
public class EventOperator extends Event {
    @Enumerated(EnumType.STRING)
    private OperatorCategory category;
    private Instant startDate;
    private Instant endDate;

    public EventOperator() {
        super();
    }

    public EventOperator(UUID id) {
        super(id, EventType.OPERATOR);
    }

    public OperatorCategory getCategory() {
        return category;
    }

    public void setCategory(OperatorCategory category) {
        this.category = category;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }
}
