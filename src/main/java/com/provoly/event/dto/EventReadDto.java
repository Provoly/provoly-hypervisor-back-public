package com.provoly.event.dto;

import java.time.Instant;
import java.util.UUID;

import com.provoly.equipment.EquipmentReadDto;
import com.provoly.event.Category;
import com.provoly.event.Criticality;
import com.provoly.event.EventType;
import com.provoly.event.Status;

public class EventReadDto {
    private UUID id;
    private String name;
    private String address;
    private String description;
    private Criticality criticality;
    private Category category;
    private Status status;
    private EventType type;
    private Instant lastModificationDate;
    private Instant creationDate;
    private Instant closeDate;
    private EquipmentReadDto equipment;
    private UUID procedureId;
    private long linkedEvents;
    private float procedureProgress;
    private String domain;

    public EventReadDto(UUID id,
            String name,
            String address,
            String description,
            Criticality criticality, Category category,
            Status status,
            EventType type,
            Instant lastModificationDate,
            Instant creationDate,
            Instant closeDate,
            EquipmentReadDto equipment,
            UUID procedureId,
            long linkedEvents,
            float procedureProgress,
            String domain) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.criticality = criticality;
        this.category = category;
        this.status = status;
        this.type = type;
        this.lastModificationDate = lastModificationDate;
        this.creationDate = creationDate;
        this.closeDate = closeDate;
        this.equipment = equipment;
        this.procedureId = procedureId;
        this.linkedEvents = linkedEvents;
        this.procedureProgress = procedureProgress;
        this.domain = domain;
    }

    public EventReadDto(EventReadDto dto) {
        this.id = dto.getId();
        this.name = dto.getName();
        this.address = dto.getAddress();
        this.description = dto.getDescription();
        this.criticality = dto.getCriticality();
        this.status = dto.getStatus();
        this.type = dto.getType();
        this.lastModificationDate = dto.getLastModificationDate();
        this.creationDate = dto.getCreationDate();
        this.closeDate = dto.getCloseDate();
        this.equipment = dto.getEquipment();
        this.procedureId = dto.getProcedureId();
        this.linkedEvents = dto.getLinkedEvents();
        this.procedureProgress = dto.getProcedureProgress();
        this.domain = dto.getDomain();
        this.category = dto.getCategory();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public Category getCategory() {
        return category;
    }

    public Status getStatus() {
        return status;
    }

    public EventType getType() {
        return type;
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public Instant getCloseDate() {
        return closeDate;
    }

    public EquipmentReadDto getEquipment() {
        return equipment;
    }

    public UUID getProcedureId() {
        return procedureId;
    }

    public long getLinkedEvents() {
        return linkedEvents;
    }

    public float getProcedureProgress() {
        return procedureProgress;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}