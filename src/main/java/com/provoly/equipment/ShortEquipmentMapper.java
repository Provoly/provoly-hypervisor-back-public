package com.provoly.equipment;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ShortEquipmentMapper {

    public EquipmentShortDto mapToEquipmentShortDto(Equipment equipment) {
        if (equipment == null) {
            return null;
        }
        return new EquipmentShortDto(
                equipment.getId(),
                equipment.getCode(),
                equipment.getEntity().getName(),
                equipment.getFamily().getName());
    }

}
