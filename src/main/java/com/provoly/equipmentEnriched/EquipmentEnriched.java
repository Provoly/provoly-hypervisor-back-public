package com.provoly.equipmentEnriched;

import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.IN_PROGRESS;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.provoly.equipment.Equipment;
import com.provoly.event.Event;
import com.provoly.event.Status;
import com.provoly.service.Service;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

public class EquipmentEnriched {
    private final UUID id;
    private final String name;
    private final String code;
    private final String domain;
    private final String entity;
    private final String family;
    private final String place;
    private final String deleted;
    private final Map<String, Object> attributes;
    private final EquipmentEnriched parent;
    private final List<CondensedEvent> events;
    private final long nbServicesAskedInProgress;
    private final List<CondensedService> services;

    public EquipmentEnriched(Equipment equipment) {
        this.id = equipment.getId();
        this.name = equipment.getName();
        this.code = equipment.getCode();
        this.domain = equipment.getDomain().getCode();
        this.entity = equipment.getEntity().getCode();
        this.family = equipment.getFamily().getCode();
        this.attributes = equipment.getAttributes();
        this.place = equipment.getDistrict().getCode();
        this.deleted = String.valueOf(equipment.isDeleted());
        this.parent = equipment.getParent() == null ? null : new EquipmentEnriched(equipment.getParent());
        this.events = equipment
                .getEvents()
                .stream()
                .filter(event -> event.getStatus() != Status.DONE)
                .map(this::condensedEvent)
                .toList();
        this.nbServicesAskedInProgress = equipment
                .getServices()
                .stream()
                .filter(service -> List.of(ASKED, IN_PROGRESS).contains(service.getStatus()))
                .count();
        this.services = equipment
                .getServices()
                .stream()
                .map(this::condensedService)
                .toList();
    }

    public EquipmentEnriched(UUID id, String externalId, String name, String code, String domain, String entity, String family,
            String place, boolean deleted, Map<String, Object> attributes, EquipmentEnriched parent,
            List<CondensedEvent> events,
            long nbServicesAskedInProgress, List<CondensedService> services) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.domain = domain;
        this.entity = entity;
        this.family = family;
        this.place = place;
        this.deleted = String.valueOf(deleted);
        this.attributes = attributes;
        this.parent = parent;
        this.events = events;
        this.nbServicesAskedInProgress = nbServicesAskedInProgress;
        this.services = services;
    }

    public UUID getId() {
        return id;
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

    public String getPlace() {
        return place;
    }

    public String getDeleted() {
        return deleted;
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

    public List<CondensedService> getServices() {
        return services;
    }

    private CondensedEvent condensedEvent(Event event) {
        return new CondensedEvent(
                event.getCategory().getCode(),
                event.getCriticality());
    }

    private CondensedService condensedService(Service service) {
        return new CondensedService(service.getCategory().getCode());
    }

}
