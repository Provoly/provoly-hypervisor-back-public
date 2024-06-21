package com.provoly.action;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import com.provoly.event.Status;

@Entity
@DiscriminatorValue("ASKED_SERVICE")
public class AskedService extends Action {
    // TODO: add reference to service ?
    public AskedService() {
        super();
    }

    public AskedService(UUID id) {
        super(id);
    }

    public AskedService(UUID id, Instant lastModificationDate, Status status, String name) {
        super(id, ActionType.ASKED_SERVICE, lastModificationDate, status, name);
    }

}
