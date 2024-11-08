package com.provoly.service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;

import com.provoly.equipment.Equipment;
import com.provoly.event.Domain;

@Entity
public class Service {
    @Id
    private UUID id;

    private String externalId;
    private String additionalInfo;
    private String description;

    @Column(updatable = false)
    private Instant creationDate;

    private Instant lastModificationDate;

    private Instant startDate;

    private Instant endDate;

    private Instant closeDate;

    @ManyToOne
    private Equipment equipment;

    @ManyToOne
    private Domain domain;

    @Enumerated(EnumType.STRING)
    private ServiceStatus status;

    @ManyToOne
    private ServiceCategory category;

    protected Service() {
        // Only for JPA
    }

    public Service(UUID id, String externalId, String additionalInfo, Instant creationDate, Instant lastModificationDate,
            Instant startDate,
            Instant endDate, Instant closeDate, Equipment equipment, Domain domain, ServiceStatus status,
            ServiceCategory category) {
        this.id = id;
        this.externalId = externalId;
        this.additionalInfo = additionalInfo;
        this.creationDate = creationDate;
        this.lastModificationDate = lastModificationDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.closeDate = closeDate;
        this.equipment = equipment;
        this.domain = domain;
        this.status = status;
        this.category = category;
    }

    public Service(UUID id, String externalId) {
        this.id = id;
        this.externalId = externalId;
    }

    public UUID getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Instant lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
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

    public Instant getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Instant closeDate) {
        this.closeDate = closeDate;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        if (equipment != null) {
            equipment.addService(this);
        }
        this.equipment = equipment;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Service entityId = (Service) o;
        return id.equals(entityId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
