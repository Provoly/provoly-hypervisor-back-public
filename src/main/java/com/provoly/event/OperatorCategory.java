package com.provoly.event;

public enum OperatorCategory {
    MANIFESTATION,
    OPERATOR_EVENT;

    public static OperatorCategory fromString(String category) {
        return category == null ? null : valueOf(category);
    }

    public static boolean isOperatorCategory(String category) {
        try {
            valueOf(category);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
