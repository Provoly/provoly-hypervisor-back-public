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

    public OtherAction(UUID id, Status status, String name, int order) {
        super(id, ActionType.OTHER.name(), status, order);
        this.name = name;
    }

    public OtherAction(int order, String name) {
        super(order, ActionType.OTHER.name());
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
