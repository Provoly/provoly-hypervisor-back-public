package com.provoly.action;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EMAIL")
public class EmailAction extends Action {
    private String name;
    private String email;

    public EmailAction() {
    }

    public EmailAction(int order, String name, String email) {
        super(order, ActionType.EMAIL.name());
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
