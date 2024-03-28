package com.provoly.event;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.DatabaseReader;
import com.provoly.equipment.EquipmentMapper;
import com.provoly.equipment.EquipmentService;
import com.provoly.event.dto.*;
import com.provoly.procedure.Procedure;
import com.provoly.procedure.ProcedureService;

@ApplicationScoped
public class EventMapper {
    private EquipmentMapper equipmentMapper;
    private ProcedureService procedureService;
    private EquipmentService equipmentService;
    private DatabaseReader databaseReader;

    public EventMapper(EquipmentService equipmentService, EquipmentMapper equipmentMapper,
            ProcedureService procedureService, DatabaseReader databaseReader) {
        this.equipmentService = equipmentService;
        this.equipmentMapper = equipmentMapper;
        this.procedureService = procedureService;
        this.databaseReader = databaseReader;
    }

    public EventReadDto mapToEventReadDto(Event event) {
        var eventDto = new EventReadDto(
                event.getId(),
                event.getName(),
                event.getAddress(),
                event.getDescription(),
                event.getCriticality(),
                event.getStatus(),
                event.getType(),
                event.getLastModificationDate(),
                event.getCreationDate(),
                event.getCloseDate(),
                equipmentMapper.mapToEquipmentReadDto(event.getEquipment()),
                getProcedureId(event),
                procedureService.getLinkedEventCountByProcedure(event.getProcedure()),
                getProgressActions(event.getProcedure()),
                mapToString(event.getDomain()));

        return switch (event) {
            case EventOperator e -> new OperatorEventReadDto(eventDto, e.getCategory(), e.getStartDate(), e.getEndDate());
            case EventAlert e -> new AlertEventReadDto(eventDto, e.getExternalSourceRef(), e.getCategory());
            case EventReport e -> new ReportEventReadDto(eventDto, e.getExternalSourceRef(), e.getCategory());
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };

    }

    public EventSummaryDto mapToEventSummaryDto(Event event, int serviceCount, String serviceTitle) {
        return new EventSummaryDto(event.getId(),
                event.getName(),
                event.getCriticality(),
                event.getStatus(),
                event.getType(),
                event.getLastModificationDate(),
                getCategory(event),
                serviceTitle,
                (long) serviceCount,
                getManifestationDate(event),
                event.getProcedure() == null ? null : event.getProcedure().getId());
    }

    public void updateOperatorEvent(OperatorEventWriteDto dto, EventOperator entity) {
        setCommonEventProperties(dto, entity);
        entity.setCategory(dto.getCategory());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
    }

    public void saveAlertEvent(AlertEventWriteDto dto, EventAlert entity) {
        setCommonEventProperties(dto, entity);
        entity.setCategory(dto.getCategory());
        entity.setExternalSourceRef(dto.getExternalSourceRef());
    }

    public void updateReportEvent(ReportEventWriteDto dto, EventReport entity) {
        setCommonEventProperties(dto, entity);
        entity.setCategory(dto.getCategory());
        entity.setExternalSourceRef(dto.getExternalSourceRef());
    }

    public Collection<EventReadDto> mapToEventReadDto(Collection<Event> events) {
        return events.stream().map(this::mapToEventReadDto).toList();
    }

    private void setCommonEventProperties(EventWriteDto dto, Event entity) {
        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());
        entity.setDescription(dto.getDescription());
        entity.setCriticality(dto.getCriticality());
        entity.setEquipment(equipmentService.getEquipmentByIdOrNull(dto.getEquipmentId()));
        entity.setDomain(mapToDomain(dto.getDomain()));
    }

    private String mapToString(Domain domain) {
        return domain != null ? domain.getName() : null;
    }

    private Domain mapToDomain(String domain) {
        if (domain == null) {
            return null;
        }
        return databaseReader.getDomainByName(domain)
                .orElseThrow(() -> new IllegalArgumentException("Domain with name %s invalid".formatted(domain)));
    }

    private float getProgressActions(Procedure procedure) {
        return procedure == null ? 0 : procedure.getProcedureProgress();
    }

    private UUID getProcedureId(Event event) {
        return event.getProcedure() != null ? event.getProcedure().getId() : null;
    }

    private Map<String, Instant> getManifestationDate(Event event) {
        if (event.getType() == EventType.OPERATOR) {
            EventOperator eventOp = (EventOperator) event;
            if (eventOp.getCategory() == OperatorCategory.MANIFESTATION) {
                return Map.of("startDate", eventOp.getStartDate(), "endDate", eventOp.getEndDate());
            }
        }
        return null;
    }

    private String getCategory(Event event) {
        return switch (event) {
            case EventOperator e -> e.getCategory().name();
            case EventAlert e -> e.getCategory().name();
            case EventReport e -> e.getCategory().name();
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };
    }
}
