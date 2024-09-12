package com.provoly.service.coswin;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ServiceType {
    @Id
    private Long id;
    private String type;
    private String domain;
    private Integer gti;
    private Integer gtr;
    private Integer gtrp;

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDomain() {
        return domain;
    }

    public Integer getGti() {
        return gti;
    }

    public Integer getGtr() {
        return gtr;
    }

    public Integer getGtrp() {
        return gtrp;
    }
}
