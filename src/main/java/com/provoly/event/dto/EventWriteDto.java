package com.provoly.event.dto;

import static com.provoly.event.EventMapper.DEFAULT_SOURCE;

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

    private final String externalSourceRef;
    private final String externalId;
    private final Integer parent;

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
            String externalSourceRef,
            String externalId,
            Integer parent) {
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
        this.externalSourceRef = externalSourceRef;
        this.creationDate = creationDate;
        this.externalId = externalId;
        this.parent = parent;
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
            String externalSourceRef) {
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
        this.externalSourceRef = externalSourceRef;
        this.externalId = null;
        this.parent = null;
        this.creationDate = null;
    }

    public Integer getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
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

    public String getExternalSourceRef() {
        return externalSourceRef == null || externalSourceRef.isBlank() ? DEFAULT_SOURCE : externalSourceRef;
    }

    public Integer getParent() {
        return parent;
    }
}
