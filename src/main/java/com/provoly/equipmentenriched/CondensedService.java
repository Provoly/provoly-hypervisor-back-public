package com.provoly.equipmentenriched;

import com.fasterxml.jackson.annotation.JsonCreator;

public record CondensedService(String category) {

    @JsonCreator
    public CondensedService(String category) {
        this.category = category;
    }

}
