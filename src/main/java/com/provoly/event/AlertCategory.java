package com.provoly.event;

public enum AlertCategory {
    ALERT_LIMIT,
    ALERT_MALFUNCTION;

    public static AlertCategory fromString(String category) {
        return category == null ? null : valueOf(category);
    }

    public static boolean isAlertCategory(String category) {
        try {
            valueOf(category);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
