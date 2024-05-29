package com.provoly.service;

public enum ServiceStatus {
    CREATED(1),
    ASKED(2),
    IN_PROGRESS(3),
    DONE(4),
    CANCELLED(5);

    private final int priority;

    ServiceStatus(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static ServiceStatus fromString(String status) {
        return status == null ? null : valueOf(status);
    }
}
