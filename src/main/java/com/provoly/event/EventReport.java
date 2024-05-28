package com.provoly.event;

import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REPORT")
public class EventReport extends Event {

    private String externalSourceRef;

    public EventReport() {
        super();
    }

    public EventReport(UUID id) {
        super(id, EventType.REPORT);
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

    public void setExternalSourceRef(String externalSourceRef) {
        this.externalSourceRef = externalSourceRef;
    }
}
