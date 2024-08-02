package com.provoly.action;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SMS")
public class SmsAction extends PhoneAction {
    public SmsAction() {
        super();
    }

    public SmsAction(int order, String name, String number) {
        super(order, name, number);
    }
}
