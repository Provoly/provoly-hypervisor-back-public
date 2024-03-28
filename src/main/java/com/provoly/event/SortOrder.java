package com.provoly.event;

public enum SortOrder {
    ASC,
    DESC;

    public static SortOrder fromString(String order) {
        return order == null ? null : valueOf(order);
    }
}
