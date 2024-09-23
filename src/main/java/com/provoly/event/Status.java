package com.provoly.event;

public enum Status {
    NEW(1, "Nouveau"),
    IN_PROGRESS(2, "En cours"),
    DONE(3, "Clôturé");

    private final int priority;
    private final String name;

    Status(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public static Status fromString(String status) {
        return status == null ? null : valueOf(status);
    }
}
