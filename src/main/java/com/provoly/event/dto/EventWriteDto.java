package com.provoly.event.dto;

import java.util.Objects;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.provoly.event.Category;
import com.provoly.event.Criticality;
import com.provoly.event.EventType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReportEventWriteDto.class, name = "REPORT"),
        @JsonSubTypes.Type(value = AlertEventWriteDto.class, name = "ALERT"),
        @JsonSubTypes.Type(value = OperatorEventWriteDto.class, name = "OPERATOR"),
})
public abstract class EventWriteDto {
    @NotNull
    private UUID id;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String description;

    @NotNull
    private Criticality criticality;

    @NotNull
    protected Category category;

    private String address;

    private UUID equipmentId;

    private EventType type;

    private String domain;

    protected EventWriteDto(UUID id, String name, String description, Criticality criticality, Category category,
            String address, UUID equipmentId,
            EventType type, String domain) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.criticality = criticality;
        this.category = category;
        this.address = address;
        this.equipmentId = equipmentId;
        this.type = type;
        this.domain = domain;
    }

    @AssertTrue(message = "Category is invalid")
    public boolean isValidCategory() {
        return Objects.nonNull(category) && category.getEventType() == type;
    }

    public UUID getId() {
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

    public Category getCategory() {
        return category;
    }

    public String getAddress() {
        return address;
    }

    public UUID getEquipmentId() {
        return equipmentId;
    }

    public EventType getType() {
        return type;
    }

    public String getDomain() {
        return domain;
    }
}
