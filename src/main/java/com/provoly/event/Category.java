package com.provoly.event;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import com.provoly.EnumEntity;

@Entity
public class Category extends EnumEntity {
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    public Category getParent() {
        return parent;
    }
}