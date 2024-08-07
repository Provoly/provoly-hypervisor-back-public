package com.provoly.service;

public enum ServiceStatus {
    ASKED(1),
    IN_PROGRESS(2),
    DONE(3),
    CANCELLED(4);

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
