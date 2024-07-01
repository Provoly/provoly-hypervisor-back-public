package com.provoly.action;

import java.util.UUID;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import com.provoly.event.Status;

@Entity
@DiscriminatorValue("SMS")
public class SmsAction extends PhoneAction {
    public SmsAction() {
        super();
    }

    public SmsAction(UUID id, String type, Status status, String name, String phone) {
        super(id, type, status, name, phone);
    }
}
