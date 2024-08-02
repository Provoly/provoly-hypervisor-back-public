package com.provoly.action;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PHONE")
public class PhoneAction extends Action {
    private String name;
    private String number;

    public PhoneAction() {
        super();
    }

    public PhoneAction(int order, String name, String number) {
        super(order, ActionType.PHONE.name());
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
