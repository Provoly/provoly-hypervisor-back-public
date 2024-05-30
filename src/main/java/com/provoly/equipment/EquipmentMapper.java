package com.provoly.equipment;

import java.util.Collection;
import java.util.Comparator;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.event.Event;
import com.provoly.event.EventMapper;
import com.provoly.service.Service;
import com.provoly.service.ServiceMapper;

@ApplicationScoped
public class EquipmentMapper {

    public static final int MAX_SIZE = 5;
    private final EventMapper eventMapper;
    private final EquipmentDatabaseReader databaseReader;
    private final ServiceMapper serviceMapper;

    public EquipmentMapper(EventMapper eventMapper, EquipmentDatabaseReader databaseReader, ServiceMapper serviceMapper) {
        this.eventMapper = eventMapper;
        this.databaseReader = databaseReader;
        this.serviceMapper = serviceMapper;
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
                equipment.getDomain().getCode(),
                equipment.getFamily().getCode(),
                equipment.getEntity().getName(),
                equipment.getAttributes(),
                mapToEquipmentReadDto(equipment.getParent()),
                serviceMapper.mapToServiceReadDtos(equipment.getServices().stream()
                        .sorted(Comparator.comparing(Service::getLastModificationDate)).limit(MAX_SIZE).toList()),
                eventMapper.mapToEventReadDto(equipment.getEvents().stream()
                        .sorted(Comparator.comparing(Event::getLastModificationDate)).limit(MAX_SIZE).toList()));
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
                .getDomainByCode(dto.domain())
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
