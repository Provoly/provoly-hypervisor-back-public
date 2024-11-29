package com.provoly.event.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.Criticality;

import com.fasterxml.jackson.annotation.JsonCreator;

public class EventWriteDto {
    private final Integer id;

    @NotNull
    @NotBlank
    private final String name;

    @NotNull
    @NotBlank
    private final String description;

    @NotNull
    private final Criticality criticality;

    @NotNull
    private final String category;

    private final String subCategory;

    private final String address;

    private final UUID equipmentId;

    private final String domain;

    private final Instant startDate;

    private final Instant endDate;

    private final Instant creationDate;

    private final Integer parent;

    private final String externalSourceRef;

    private final String externalId;

    private final String creator;

    @JsonCreator
    public EventWriteDto(Integer id,
            String name,
            String description,
            Criticality criticality,
            String category,
            String subCategory,
            String address,
            UUID equipmentId,
            String domain,
            Instant startDate,
            Instant endDate,
            Instant creationDate,
            Integer parent,
            String creator,
            String externalSourceRef,
            String externalId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.criticality = criticality;
        this.category = category;
        this.subCategory = subCategory;
        this.address = address;
        this.equipmentId = equipmentId;
        this.domain = domain;
        this.startDate = startDate;
        this.endDate = endDate;
        this.creationDate = creationDate;
        this.parent = parent;
        this.externalSourceRef = externalSourceRef;
        this.externalId = externalId;
        this.creator = creator;
    }

    public EventWriteDto(Integer id,
            String name,
            String description,
            Criticality criticality,
            String category,
            String subCategory,
            String address,
            UUID equipmentId,
            String domain,
            Instant startDate,
            Instant endDate,
            Instant creationDate,
            Integer parent,
            String creator) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.criticality = criticality;
        this.category = category;
        this.subCategory = subCategory;
        this.address = address;
        this.equipmentId = equipmentId;
        this.domain = domain;
        this.startDate = startDate;
        this.endDate = endDate;
        this.creationDate = creationDate;
        this.parent = parent;
        this.externalSourceRef = null;
        this.externalId = null;
        this.creator = creator;
    }

    public EventWriteDto(Integer id,
            String name,
            String description,
            Criticality criticality,
            String category,
            String subCategory,
            String address,
            UUID equipmentId,
            String domain,
            Instant startDate,
            Instant endDate,
            Instant creationDate,
            Integer parent,
            String externalSourceRef,
            String externalId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.criticality = criticality;
        this.category = category;
        this.subCategory = subCategory;
        this.address = address;
        this.equipmentId = equipmentId;
        this.domain = domain;
        this.startDate = startDate;
        this.endDate = endDate;
        this.creationDate = creationDate;
        this.parent = parent;
        this.externalSourceRef = externalSourceRef;
        this.externalId = externalId;
        this.creator = null;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getAddress() {
        return address;
    }

    public UUID getEquipmentId() {
        return equipmentId;
    }

    public String getDomain() {
        return domain;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Integer getParent() {
        return parent;
    }

    public String getExternalSourceRef() {
        return externalSourceRef;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getCreator() {
        return creator;
    }
}
