package com.provoly.equipment;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public record EquipmentWriteDto(
        @NotNull Map<String, String> id,
        @NotNull int level,
        @NotNull String name,
        @NotNull String code,
        @NotNull String domain,
        @NotNull String family,
        @NotNull String entity,
        @NotNull String city,
        @NotNull String address,
        @NotNull String district,
        String parent,
        boolean deleted,
        Map<String, Object> attributes) {

    public EquipmentWriteDto {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
    }

    public EquipmentWriteDto(String valueId, int level, String name, String code, String domain, String family, String entity,
            String city, String address, String district, String parent, boolean deleted, Map<String, Object> attributes) {
        this(Map.of(valueId, valueId), level, name, code, domain, family, entity, city, address, district, parent, deleted,
                attributes);
    }

    @JsonAnySetter
    public void setAttributes(String name, Object value) {
        attributes.put(name, value);
    }
}
