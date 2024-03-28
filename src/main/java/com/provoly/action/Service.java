package com.provoly.action;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.provoly.equipment.Equipment;
import com.provoly.event.Status;

@Entity
@DiscriminatorValue("SERVICE")
public class Service extends Action {

    @ManyToOne
    private Equipment equipment;

    public Service() {
        super();
    }

    public Service(UUID id, Instant lastModificationDate, Status status, String name) {
        super(id, ActionType.SERVICE, lastModificationDate, status, name);
    }
}
