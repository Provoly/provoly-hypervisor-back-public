package com.provoly.action;

import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import com.provoly.event.Status;

@Entity
@DiscriminatorValue("ASKED_SERVICE")
public class AskedService extends Action {
    private String name;
    private String serviceExternalId;

    public AskedService() {
        super();
    }

    public AskedService(Action action, String name, String serviceExternalId) {
        super(action.getId(), ActionType.ASKED_SERVICE.name(), action.getStatus());
        this.name = name;
        this.serviceExternalId = serviceExternalId;
    }

    public AskedService(UUID id, Status status, String name) {
        super(id, ActionType.ASKED_SERVICE.name(), status);
        this.name = name;
        this.serviceExternalId = null;
    }

    public String getName() {
        return name;
    }

    public String getServiceExternalId() {
        return serviceExternalId;
    }

}
