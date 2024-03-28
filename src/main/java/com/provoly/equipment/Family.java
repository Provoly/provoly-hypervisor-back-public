package com.provoly.equipment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Family {
    @Id
    private Long id;

    private String name;
    private String code;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "%s/%s".formatted(name, code);
    }
}
