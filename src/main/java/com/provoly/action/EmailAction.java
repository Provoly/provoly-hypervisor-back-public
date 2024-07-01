package com.provoly.action;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EMAIL")
public class EmailAction extends Action {
    private String name;
    private String email;

    public EmailAction() {
        super();
    }

    public EmailAction(Action action, String name, String email) {
        super(action.getId(), ActionType.EMAIL.name(), action.getStatus());
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

}
