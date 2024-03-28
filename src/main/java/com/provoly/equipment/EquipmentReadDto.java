package com.provoly.equipment;

import java.util.UUID;

public record EquipmentReadDto(
        UUID id,
        String name,
        String type,
        String entity) {
}
