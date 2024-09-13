package com.provoly.event;

import java.util.Arrays;

public enum EventSort {
    CREATION_DATE("creationDate"),
    LAST_MODIFICATION_DATE("lastModificationDate"),
    STATUS("status"),
    PROCEDURE_PROGRESS("procedureProgress"),
    NAME("name"),
    CRITICALITY("criticality"),
    CATEGORY("category"),
    ID("id"),
    SOURCE("source");

    private final String name;

    EventSort(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EventSort fromName(String name) {
        return name == null ? null
                : Arrays.stream(values())
                        .filter(sort -> sort.getName().equals(name))
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException("It's not possible to sort on property %s".formatted(name)));
    }
}
