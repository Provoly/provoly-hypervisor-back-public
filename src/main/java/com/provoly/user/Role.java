package com.provoly.user;

public enum Role {
    EVENT_WRITE,
    EVENT_READ,
    EVENT_PROC_WRITE,
    PROC_MODEL_READ,
    PROC_MODEL_WRITE;

    public static final String STR_EVENT_WRITE = "event_write";
    public static final String STR_EVENT_READ = "event_read";
    public static final String STR_EVENT_PROC_WRITE = "event_proc_write";
    public static final String STR_PROC_MODEL_READ = "proc_model_read";
    public static final String STR_PROC_MODEL_WRITE = "proc_model_write";

    public static boolean exists(String role) {
        for (Role value : values()) {
            if (value.name().toLowerCase().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
