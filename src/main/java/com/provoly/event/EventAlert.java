package com.provoly.event;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ALERT")
public class EventAlert extends Event {
    private String externalSourceRef;

    public EventAlert() {
        super(EventType.ALERT);
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

    public void setExternalSourceRef(String externalSourceRef) {
        this.externalSourceRef = externalSourceRef;
    }
}
