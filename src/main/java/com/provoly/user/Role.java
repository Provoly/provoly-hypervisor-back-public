package com.provoly.user;

public enum Role {
    EVENT_WRITE,
    EVENT_READ,
    EVENT_PROC_WRITE,
    EVENT_PROC_COMMENT_WRITE,
    PROC_MODEL_READ,
    PROC_MODEL_WRITE,
    EQUIPMENT_WRITE,
    EQUIPMENT_READ,
    SERVICE_WRITE,
    SERVICE_READ,
    SERVICE_EXTERNAL_WRITE,
    METRIC_READ,
    INTERNAL_DEBUG;

    public static final String STR_EVENT_WRITE = "event_write";
    public static final String STR_EVENT_READ = "event_read";
    public static final String STR_EVENT_PROC_WRITE = "event_proc_write";
    public static final String STR_EVENT_PROC_COMMENT_WRITE = "event_proc_comment_write";
    public static final String STR_PROC_MODEL_READ = "proc_model_read";
    public static final String STR_PROC_MODEL_WRITE = "proc_model_write";
    public static final String STR_EQUIPMENT_WRITE = "equipment_write";
    public static final String STR_EQUIPMENT_READ = "equipment_read";
    public static final String STR_SERVICE_WRITE = "service_write";
    public static final String STR_SERVICE_READ = "service_read";
    public static final String STR_SERVICE_EXTERNAL_WRITE = "service_external_write";
    public static final String STR_METRIC_READ = "metric_read";
    public static final String STR_INTERNAL_DEBUG = "internal_debug";

    public static boolean exists(String role) {
        for (Role value : values()) {
            if (value.name().toLowerCase().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
