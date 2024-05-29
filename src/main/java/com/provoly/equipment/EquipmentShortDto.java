package com.provoly.equipment;

import java.util.UUID;

public record EquipmentShortDto(UUID id, String code, String entity, String family) {
}
