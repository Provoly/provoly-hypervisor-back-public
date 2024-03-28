package com.provoly.action;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import com.provoly.event.Status;

@Entity
@DiscriminatorValue("TODO")
public class TodoAction extends Action {
    public TodoAction() {
        super();
    }

    public TodoAction(UUID id, Instant lastModificationDate, Status status, String name) {
        super(id, ActionType.TODO, lastModificationDate, status, name);
    }
}
