package com.provoly.equipment;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EquipmentMapper {

    private EquipmentDatabaseReader databaseReader;

    public EquipmentMapper(EquipmentDatabaseReader databaseReader) {
        this.databaseReader = databaseReader;
    }

    public EquipmentReadDto mapToEquipmentReadDto(Equipment equipment) {
        if (equipment == null) {
            return null;
        }
        return new EquipmentReadDto(
                equipment.getId(),
                equipment.getExternalId(),
                equipment.getName(),
                equipment.getCode(),
                equipment.getDomain().getName(),
                equipment.getFamily().getName(),
                equipment.getEntity().getName(),
                equipment.getAttributes(),
                mapToEquipmentReadDto(equipment.getParent()));
    }

    public Collection<EquipmentReadDto> mapToEquipmentReadDto(Collection<Equipment> equipments) {
        return equipments.stream().map(this::mapToEquipmentReadDto).toList();
    }

    public void updateEquipment(EquipmentWriteDto dto, Equipment entity) {
        entity.setExternalId(dto.id());
        entity.setName(dto.name());
        entity.setCode(dto.code());
        entity.setEntity(mapToEntity(dto.entity()));
        entity.setFamily(mapToFamily(dto.family()));

        var domain = databaseReader
                .getDomainByName(dto.domain())
                .orElseThrow(() -> new IllegalArgumentException("Domain name %s not found".formatted(dto.domain())));

        entity.setDomain(domain);

        if (dto.parent() != null) {
            var equipmentParent = databaseReader
                    .getEquipmentWithCode(dto.parent())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Equipment parent with code %s not found".formatted(dto.parent())));
            entity.setParent(equipmentParent);
        }

        for (var attr : dto.attributes().entrySet()) {
            entity.getAttributes().put(attr.getKey(), attr.getValue());
        }
    }

    public EquipmentEntity mapToEntity(String name) {
        return databaseReader.getEquipmentEntityByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Equipment entity with name %s invalid".formatted(name)));
    }

    public Family mapToFamily(String name) {
        return databaseReader.getFamilyByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Equipment family with name %s invalid".formatted(name)));
    }
}
