package com.provoly.action;

import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import com.provoly.event.Status;

@Entity
@DiscriminatorValue("OTHER")
public class OtherAction extends Action {
    private String name;

    public OtherAction() {
        super();
    }

    public OtherAction(Action action, String name) {
        super(action.getId(), ActionType.OTHER.name(), action.getStatus());
        this.name = name;
    }

    public OtherAction(UUID id, Status status, String name) {
        super(id, ActionType.OTHER.name(), status);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
