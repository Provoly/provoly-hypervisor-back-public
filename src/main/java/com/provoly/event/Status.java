package com.provoly.event;

public enum Status {
    NEW(1),
    IN_PROGRESS(2),
    DONE(3);

    private final int priority;

    Status(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static Status fromString(String status) {
        return status == null ? null : valueOf(status);
    }
}
