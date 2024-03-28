package com.provoly.event;

import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("REPORT")
public class EventReport extends Event {
    @Enumerated(EnumType.STRING)
    private ReportCategory category;

    private String externalSourceRef;

    public EventReport() {
        super();
    }

    public EventReport(UUID id) {
        super(id, EventType.REPORT);
    }

    public ReportCategory getCategory() {
        return category;
    }

    public void setCategory(ReportCategory category) {
        this.category = category;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

    public void setExternalSourceRef(String externalSourceRef) {
        this.externalSourceRef = externalSourceRef;
    }
}
