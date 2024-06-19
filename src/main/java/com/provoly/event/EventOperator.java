package com.provoly.event;

import java.time.Instant;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("OPERATOR")
public class EventOperator extends Event {
    private Instant startDate;
    private Instant endDate;

    public EventOperator() {
        super(EventType.OPERATOR);
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
