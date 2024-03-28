package com.provoly.event;

import java.util.Arrays;

public enum Sort {
    CREATION_DATE("creationDate"),
    LAST_MODIFICATION_DATE("lastModificationDate"),
    STATUS("status"),
    PROCEDURE_PROGRESS("procedureProgress");

    private final String name;

    Sort(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Sort fromName(String name) {
        return name == null ? null
                : Arrays.stream(values())
                        .filter(sort -> sort.getName().equals(name))
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException("It's not possible to sort on property %s".formatted(name)));
    }
}
