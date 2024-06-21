package com.provoly.model;

import java.util.Arrays;

public enum ProcedureModelSort {
    ID("id"),
    USE_COUNT("useCount"),
    NAME("name"),
    CREATOR("creator"),
    DOMAIN("domain"),
    CREATION_DATE("creationDate"),
    LAST_MODIFICATION_DATE("lastModificationDate");

    private final String name;

    ProcedureModelSort(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ProcedureModelSort fromName(String name) {
        return name == null ? null
                : Arrays.stream(values())
                        .filter(sort -> sort.getName().equals(name))
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException("It's not possible to sort on property %s".formatted(name)));
    }
}
