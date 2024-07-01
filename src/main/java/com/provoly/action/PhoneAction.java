package com.provoly.action;

import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import com.provoly.event.Status;

@Entity
@DiscriminatorValue("PHONE")
public class PhoneAction extends Action {
    private String name;
    private String number;

    public PhoneAction() {
        super();
    }

    public PhoneAction(UUID id, String type, Status status, String name, String number) {
        super(id, type, status);
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}
