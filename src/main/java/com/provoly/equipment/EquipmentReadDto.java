package com.provoly.equipment;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

public record EquipmentReadDto(
        UUID id,
        String externalId,
        String name,
        String code,
        String domain,
        String family,
        String entity,
        Map<String, Object> attributes,
        EquipmentReadDto parent) {

    @JsonAnyGetter
    public Map<String, Object> attributes() {
        return attributes;
    }
}
