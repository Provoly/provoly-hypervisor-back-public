package com.provoly.equipment;

import java.util.*;

import jakarta.persistence.*;

import com.provoly.action.Service;
import com.provoly.event.Domain;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class Equipment {

    @Id
    private UUID id;

    private String externalId;

    private String name;

    private String code;

    @ManyToOne
    private Domain domain;

    @ManyToOne
    @JoinColumn(name = "equipment_entity_id")
    private EquipmentEntity entity;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @OneToMany(mappedBy = "equipment", fetch = FetchType.EAGER)
    Collection<Service> services = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> attributes = new HashMap<>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Equipment parent;

    public Equipment() {
        // Only for JPA
    }

    public Equipment(UUID id) {
        this.id = id;
    }

    public Equipment(UUID id, String externalId, String name, String code, Domain domain, EquipmentEntity entity, Family family,
            Collection<Service> services, Map<String, Object> attributes, Equipment parent) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.code = code;
        this.domain = domain;
        this.entity = entity;
        this.family = family;
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
}
