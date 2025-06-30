package com.provoly.action;

import com.provoly.event.Status;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.UUID;

@Entity
@DiscriminatorValue("ALERT_ELECTED")
public class AlertElected extends Action {
    private String name;
    private String serviceExternalId;

    public AlertElected() {
        super();
    }

    public AlertElected(Action action, String name, String serviceExternalId) {
        super(action.getId(), ActionType.ALERT_ELECTED.name(), action.getStatus(), action.getOrder());
        this.name = name;
        this.serviceExternalId = serviceExternalId;
    }

    public AlertElected(UUID id, Status status, String name, int order) {
        super(id, ActionType.ALERT_ELECTED.name(), status, order);
        this.name = name;
        this.serviceExternalId = null;
    }

    public AlertElected(int order, String name, String serviceExternalId) {
        super(order, ActionType.ALERT_ELECTED.name());
        this.name = name;
        this.serviceExternalId = serviceExternalId;
    }

    public AlertElected(UUID id, Status status, String name, int order, String serviceExternalId) {
        super(id, ActionType.ALERT_ELECTED.name(), status, order);
        this.name = name;
        this.serviceExternalId = serviceExternalId;
    }

    public String getName() {
        return name;
    }

    public String getServiceExternalId() {
        return serviceExternalId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServiceExternalId(String serviceExternalId) {
        this.serviceExternalId = serviceExternalId;
    }
}
