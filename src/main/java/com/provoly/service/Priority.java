package com.provoly.service;

public enum Priority {
    MINOR("1-MINEUR"),
    MAJOR("2-MAJEUR"),
    BLOCKED("3-BLOQUANT");

    private final String coswinName;

    Priority(String coswinName) {
        this.coswinName = coswinName;
    }

    public static boolean priorityExists(String coswinName) {
        for (Priority p : values()) {
            if (p.coswinName.equals(coswinName)) {
                return true;
            }
        }
        return false;
    }
}
