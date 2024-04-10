package com.provoly.event;

public enum Criticality {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    private final int priority;

    Criticality(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static Criticality fromString(String criticality) {
        return criticality == null ? null : valueOf(criticality);
    }
}
