package com.provoly.event;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REPORT")
public class EventReport extends Event {

    private String externalSourceRef;

    public EventReport() {
        super(EventType.REPORT);
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

    public void setExternalSourceRef(String externalSourceRef) {
        this.externalSourceRef = externalSourceRef;
    }
}
