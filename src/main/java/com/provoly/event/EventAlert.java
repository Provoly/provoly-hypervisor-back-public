package com.provoly.event;

import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("ALERT")
public class EventAlert extends Event {
    @Enumerated(EnumType.STRING)
    private AlertCategory category;

    private String externalSourceRef;

    public EventAlert() {
        super();
    }

    public EventAlert(UUID id) {
        super(id, EventType.ALERT);
    }

    public AlertCategory getCategory() {
        return category;
    }

    public void setCategory(AlertCategory category) {
        this.category = category;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

    public void setExternalSourceRef(String externalSourceRef) {
        this.externalSourceRef = externalSourceRef;
    }
}
