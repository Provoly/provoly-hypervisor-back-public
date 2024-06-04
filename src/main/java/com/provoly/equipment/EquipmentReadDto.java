package com.provoly.equipment;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.provoly.event.dto.EventReadDto;
import com.provoly.service.ServiceReadDto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

public record EquipmentReadDto(
        UUID id,
        String externalId,
        String name,
        String code,
        String domain,
        String family,
        String entity,
        String city,
        String address,
        String district,
        Map<String, Object> attributes,
        EquipmentReadDto parent,
        Collection<ServiceReadDto> services,
        Collection<EventReadDto> events) {

    @JsonAnyGetter
    public Map<String, Object> attributes() {
        return attributes;
    }
}
