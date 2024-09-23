package com.provoly.event;

public enum Criticality {
    HIGH(1, "Haute"),
    MEDIUM(2, "Moyenne"),
    LOW(3, "Faible");

    private final int priority;
    private final String name;

    Criticality(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public static Criticality fromString(String criticality) {
        return criticality == null ? null : valueOf(criticality);
    }
}
