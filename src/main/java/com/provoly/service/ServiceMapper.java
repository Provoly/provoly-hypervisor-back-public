package com.provoly.service;

import java.util.Collection;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.equipment.EquipmentService;
import com.provoly.equipment.ShortEquipmentMapper;

@ApplicationScoped
public class ServiceMapper {

    private final ShortEquipmentMapper shortEquipmentMapper;
    private final EquipmentService equipmentService;
    private final ServiceDatabaseReader databaseReader;

    public ServiceMapper(ShortEquipmentMapper shortEquipmentMapper, EquipmentService equipmentService,
            ServiceDatabaseReader databaseReader) {
        this.shortEquipmentMapper = shortEquipmentMapper;
        this.equipmentService = equipmentService;
        this.databaseReader = databaseReader;
    }

    public ServiceReadDto mapToServiceReadDto(Service service) {
        if (service == null) {
            return null;
        }
        return new ServiceReadDto(
                service.getId(),
                service.getExternalId(),
                service.getDescription(),
                shortEquipmentMapper.mapToEquipmentShortDto(service.getEquipment()),
                service.getCreationDate(),
                service.getLastModificationDate(),
                service.getStartDate(),
                service.getEndDate(),
                service.getDomain().getCode(),
                service.getStatus(),
                service.getCategory().getCode());
    }

    public Collection<ServiceReadDto> mapToServiceReadDtos(Stream<Service> services) {
        return services.map(this::mapToServiceReadDto).toList();
    }

    public void updateService(ServiceWriteDto dto, Service entity) {
        entity.setAdditionalInfo(dto.additionalInfo());
        entity.setDescription(dto.description());
        entity.setEquipment(dto.equipment() == null ? null : equipmentService.getEquipmentByName(dto.equipment()));
        entity.setCreationDate(dto.creationDate());
        entity.setLastModificationDate(dto.lastModificationDate());
        entity.setStartDate(dto.startDate());
        entity.setEndDate(dto.endDate());
        entity.setCloseDate(dto.closeDate());
        entity.setStatus(dto.status());
        entity.setDomain(dto.domain() == null ? null : databaseReader.getDomainByCode(dto.domain()));

        var category = databaseReader.getServiceCategoryByCode(dto.category())
                .orElseThrow(() -> new IllegalArgumentException("Service category %s not found".formatted(dto.category())));

        entity.setCategory(category);
    }
}
