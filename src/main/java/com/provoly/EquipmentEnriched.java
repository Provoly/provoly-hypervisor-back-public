package com.provoly;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.provoly.equipment.Equipment;
import com.provoly.event.Event;
import com.provoly.event.EventAlert;
import com.provoly.event.EventOperator;
import com.provoly.event.EventReport;

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
    private final List<CondensedService> services;

    public EquipmentEnriched(Equipment equipment) { // TODO: add equipment location
        this.id = equipment.getId();
        this.externalId = equipment.getExternalId();
        this.name = equipment.getName();
        this.code = equipment.getCode();
        this.domain = equipment.getDomain().getName();
        this.entity = equipment.getEntity().getName();
        this.family = "%s_%s".formatted(this.domain, equipment.getFamily().getName().replace(" ", "_").toUpperCase()); // TODO: add label in family table
        this.attributes = equipment.getAttributes();
        this.parent = equipment.getParent() == null ? null : new EquipmentEnriched(equipment.getParent());
        this.events = equipment
                .getEvents()
                .stream()
                .map(this::condensedEvent)
                .toList();
        this.services = equipment.getServices()
                .stream()
                .map(s -> new CondensedService(s.getId(), s.getStatus(), s.getLastModificationDate()))
                .toList();
    }

    public EquipmentEnriched(UUID id, String externalId, String name, String code, String domain, String entity, String family,
            Map<String, Object> attributes, EquipmentEnriched parent, List<CondensedEvent> events,
            List<CondensedService> services) {
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
        this.services = services;
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

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public EquipmentEnriched getParent() {
        return parent;
    }

    public List<CondensedEvent> getEvents() {
        return events;
    }

    public List<CondensedService> getServices() {
        return services;
    }

    private CondensedEvent condensedEvent(Event event) {
        return new CondensedEvent(
                switch (event) {
                    case EventOperator e -> e.getCategory().name();
                    case EventAlert e -> e.getCategory().name();
                    case EventReport e -> e.getCategory().name();
                    default -> throw new IllegalStateException("Unexpected value: " + event);
                },
                event.getCriticality());
    }

}
