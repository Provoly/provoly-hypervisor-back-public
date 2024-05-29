package com.provoly;

import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.IN_PROGRESS;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.provoly.equipment.Equipment;
import com.provoly.event.Event;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

public class EquipmentEnriched {
    private final UUID id;
    private final String externalId;
    private final String name;
    private final String code;
    private final String domain;
    private final String entity;
    private final String family;
    private final Map<String, Object> attributes;
    private final EquipmentEnriched parent;
    private final List<CondensedEvent> events;
    private final long nbServicesAskedInProgress;

    public EquipmentEnriched(Equipment equipment) { // TODO: add equipment location
        this.id = equipment.getId();
        this.externalId = equipment.getExternalId();
        this.name = equipment.getName();
        this.code = equipment.getCode();
        this.domain = equipment.getDomain().getCode();
        this.entity = equipment.getEntity().getCode();
        this.family = equipment.getFamily().getCode();
        this.attributes = equipment.getAttributes();
        this.parent = equipment.getParent() == null ? null : new EquipmentEnriched(equipment.getParent());
        this.events = equipment
                .getEvents()
                .stream()
                .map(this::condensedEvent)
                .toList();
        this.nbServicesAskedInProgress = equipment.getServices().stream()
                .filter(service -> List.of(ASKED, IN_PROGRESS).contains(service.getStatus()))
                .count();
    }

    public EquipmentEnriched(UUID id, String externalId, String name, String code, String domain, String entity, String family,
            Map<String, Object> attributes, EquipmentEnriched parent, List<CondensedEvent> events,
            long nbServicesAskedInProgress) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.code = code;
        this.domain = domain;
        this.entity = entity;
        this.family = family;
        this.attributes = attributes;
        this.parent = parent;
        this.events = events;
        this.nbServicesAskedInProgress = nbServicesAskedInProgress;
    }

    public UUID getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDomain() {
        return domain;
    }

    public String getEntity() {
        return entity;
    }

    public String getFamily() {
        return family;
    }

    @JsonAnyGetter
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public EquipmentEnriched getParent() {
        return parent;
    }

    public List<CondensedEvent> getEvents() {
        return events;
    }

    public long getNbServicesAskedInProgress() {
        return nbServicesAskedInProgress;
    }

    private CondensedEvent condensedEvent(Event event) {
        return new CondensedEvent(
                event.getCategory().name(),
                event.getCriticality());
    }

}
