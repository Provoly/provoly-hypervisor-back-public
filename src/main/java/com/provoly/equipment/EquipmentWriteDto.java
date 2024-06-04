package com.provoly.equipment;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public record EquipmentWriteDto(
        @NotNull String id,
        @NotNull int level,
        @NotNull String name,
        @NotNull String code,
        @NotNull String domain,
        @NotNull String family,
        @NotNull String entity,
        @NotNull String city,
        @NotNull String address,
        String district,
        String parent,
        Map<String, Object> attributes) {

    public EquipmentWriteDto {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
    }

    @JsonAnySetter
    public void setAttributes(String name, Object value) {
        attributes.put(name, value);
    }
}
