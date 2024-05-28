package com.provoly.event;

import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ALERT")
public class EventAlert extends Event {
    private String externalSourceRef;

    public EventAlert() {
        super();
    }

    public EventAlert(UUID id) {
        super(id, EventType.ALERT);
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

    public void setExternalSourceRef(String externalSourceRef) {
        this.externalSourceRef = externalSourceRef;
    }
}
