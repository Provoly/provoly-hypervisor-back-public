package com.provoly.equipment;

import java.util.*;

import jakarta.persistence.*;

import com.provoly.event.Domain;
import com.provoly.event.Event;
import com.provoly.service.Service;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class Equipment {

    @Id
    private UUID id;

    private String externalId;

    private String name;

    private String code;

    private String address;

    private boolean deleted = false;

    @ManyToOne
    private Domain domain;

    @ManyToOne
    @JoinColumn(name = "equipment_entity_id")
    private EquipmentEntity entity;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @ManyToOne
    private City city;

    @ManyToOne
    private District district;

    @OneToMany(mappedBy = "equipment", fetch = FetchType.EAGER)
    Collection<Service> services = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> attributes = new HashMap<>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Equipment parent;

    @OneToMany(mappedBy = "equipment", fetch = FetchType.EAGER)
    private List<Event> events = new ArrayList<>();

    public Equipment() {
        // Only for JPA
    }

    public Equipment(UUID id) {
        this.id = id;
    }

    public Equipment(UUID id, String externalId, String name, String code, String address, Domain domain,
            EquipmentEntity entity, Family family,
            City city, District district,
            Collection<Service> services, Map<String, Object> attributes, Equipment parent) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.code = code;
        this.address = address;
        this.domain = domain;
        this.entity = entity;
        this.family = family;
        this.city = city;
        this.district = district;
        this.services = services;
        this.attributes = attributes;
        this.parent = parent;
    }

    public UUID getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String technicalId) {
        this.externalId = technicalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public EquipmentEntity getEntity() {
        return entity;
    }

    public void setEntity(EquipmentEntity entity) {
        this.entity = entity;
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Collection<Service> getServices() {
        return services;
    }

    public void setServices(Collection<Service> services) {
        this.services = services;
    }

    public Map<String, Object> getAttributes() {
        return attributes == null ? new HashMap<>() : attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Equipment getParent() {
        return parent;
    }

    public void setParent(Equipment parent) {
        this.parent = parent;
    }

    // equipment events are managed on the event side
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void addEvent(Event event) {
        if (event.getId() == null || !events.contains(event)) { // When an event is created, it only has an id when it's persisted
            events.add(event);
        }
    }

    public void addService(Service service) {
        if (!services.contains(service)) {
            services.add(service);
        }
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
