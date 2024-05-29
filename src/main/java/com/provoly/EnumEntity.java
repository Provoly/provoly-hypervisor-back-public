package com.provoly;

import java.io.Serializable;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class EnumEntity implements Serializable {
    @Id
    protected Long id;
    protected String name;
    protected String code;

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
