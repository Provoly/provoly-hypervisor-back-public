package com.provoly.equipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import jakarta.persistence.*;

import com.provoly.action.Service;

@Entity
public class Equipment {

    @Id
    private UUID id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "equipment_entity_id")
    private EquipmentEntity entity;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    Collection<Service> services = new ArrayList<>();

    public Equipment() {
        // Only for JPA
    }

    public Equipment(UUID id, String name, EquipmentEntity entity, Family family, Collection<Service> services) {
        this.id = id;
        this.name = name;
        this.entity = entity;
        this.family = family;
        this.services = services;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EquipmentEntity getEntity() {
        return entity;
    }

    public Family getFamily() {
        return family;
    }

    public Collection<Service> getServices() {
        return services;
    }
}
