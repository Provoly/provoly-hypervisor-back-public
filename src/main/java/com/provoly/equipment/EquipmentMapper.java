package com.provoly.equipment;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EquipmentMapper {
    public EquipmentReadDto mapToEquipmentReadDto(Equipment equipment) {
        if (equipment == null) {
            return null;
        }
        return new EquipmentReadDto(
                equipment.getId(),
                equipment.getName(),
                equipment.getFamily().getName(),
                equipment.getEntity().getName());
    }

    public Collection<EquipmentReadDto> mapToEquipmentReadDto(Collection<Equipment> equipments) {
        return equipments.stream().map(this::mapToEquipmentReadDto).toList();
    }
}
