package com.provoly.event;

import java.util.Collection;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.equipment.EquipmentService;
import com.provoly.equipment.ShortEquipmentMapper;
import com.provoly.event.dto.EventReadDto;
import com.provoly.event.dto.EventSummaryDto;
import com.provoly.event.dto.EventWriteDto;
import com.provoly.procedure.Procedure;
import com.provoly.procedure.ProcedureService;

@ApplicationScoped
public class EventMapper {
    private final ProcedureService procedureService;
    private final EquipmentService equipmentService;
    private final EventDatabaseReader databaseReader;
    private final ShortEquipmentMapper shortEquipmentMapper;

    public EventMapper(EquipmentService equipmentService,
            ProcedureService procedureService, EventDatabaseReader databaseReader, ShortEquipmentMapper shortEquipmentMapper) {
        this.equipmentService = equipmentService;
        this.procedureService = procedureService;
        this.databaseReader = databaseReader;
        this.shortEquipmentMapper = shortEquipmentMapper;
    }

    public EventReadDto mapToEventReadDto(Event event) {
        return new EventReadDto(
                event.getId(),
                event.getName(),
                event.getAddress(),
                event.getDescription(),
                event.getCriticality(),
                event.getCategory().getCode(),
                event.getSubCategory() == null ? null : event.getSubCategory().getCode(),
                event.getStatus(),
                event.getLastModificationDate(),
                event.getCreationDate(),
                event.getCloseDate(),
                shortEquipmentMapper.mapToEquipmentShortDto(event.getEquipment()),
                getProcedureId(event),
                procedureService.getLinkedEventCountByProcedure(event.getProcedure()),
                getProgressActions(event.getProcedure()),
                mapToString(event.getDomain()),
                event.getStartDate(),
                event.getEndDate(),
                event.getExternalSourceRef());

    }

    public EventSummaryDto mapToEventSummaryDto(Event event, int serviceCount, String serviceTitle) {
        return new EventSummaryDto(event.getId(),
                event.getName(),
                event.getCriticality(),
                event.getStatus(),
                event.getLastModificationDate(),
                event.getCategory().getCode(),
                serviceTitle,
                (long) serviceCount,
                event.getStartDate(),
                event.getEndDate(),
                event.getProcedure() == null ? null : event.getProcedure().getId());
    }

    public Collection<EventReadDto> mapToEventReadDto(Stream<Event> events) {
        return events.map(this::mapToEventReadDto).toList();
    }

    public void updateEvent(EventWriteDto dto, Event entity) {
        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());
        entity.setDescription(dto.getDescription());
        entity.setCriticality(dto.getCriticality());
        entity.setCategory(mapToCategory(dto));
        entity.setDomain(mapToDomain(dto.getDomain()));
        entity.setEquipment(equipmentService.getEquipmentByIdOrNull(dto.getEquipmentId()));
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
    }

    private String mapToString(Domain domain) {
        return domain != null ? domain.getCode() : null;
    }

    private Domain mapToDomain(String domain) {
        if (domain == null) {
            return null;
        }
        return databaseReader.getDomainByCode(domain);
    }

    private Category mapToCategory(EventWriteDto dto) {
        var category = dto.getSubCategory() == null ? dto.getCategory() : dto.getSubCategory();
        return databaseReader.getCategoryByCode(category);
    }

    private float getProgressActions(Procedure procedure) {
        return procedure == null ? 0 : procedure.getProcedureProgress();
    }

    private Integer getProcedureId(Event event) {
        return event.getProcedure() != null ? event.getProcedure().getId() : null;
    }

}
