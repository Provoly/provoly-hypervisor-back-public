package com.provoly.event;

public enum Criticality {
    LOW,
    MEDIUM,
    HIGH;

    public static Criticality fromString(String criticality) {
        return criticality == null ? null : valueOf(criticality);
    }
}
