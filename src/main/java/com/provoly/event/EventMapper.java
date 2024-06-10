package com.provoly.event;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.equipment.EquipmentService;
import com.provoly.equipment.ShortEquipmentMapper;
import com.provoly.event.dto.*;
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
        var eventDto = new EventReadDto(
                event.getId(),
                event.getName(),
                event.getAddress(),
                event.getDescription(),
                event.getCriticality(),
                event.getCategory(),
                event.getStatus(),
                event.getType(),
                event.getLastModificationDate(),
                event.getCreationDate(),
                event.getCloseDate(),
                shortEquipmentMapper.mapToEquipmentShortDto(event.getEquipment()),
                getProcedureId(event),
                procedureService.getLinkedEventCountByProcedure(event.getProcedure()),
                getProgressActions(event.getProcedure()),
                mapToString(event.getDomain()));

        return switch (event) {
            case EventOperator e -> new OperatorEventReadDto(eventDto, e.getStartDate(), e.getEndDate());
            case EventAlert e -> new AlertEventReadDto(eventDto, e.getExternalSourceRef());
            case EventReport e -> new ReportEventReadDto(eventDto, e.getExternalSourceRef());
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
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
    }

    public void saveAlertEvent(AlertEventWriteDto dto, EventAlert entity) {
        setCommonEventProperties(dto, entity);
        entity.setExternalSourceRef(dto.getExternalSourceRef());
    }

    public void updateReportEvent(ReportEventWriteDto dto, EventReport entity) {
        setCommonEventProperties(dto, entity);
        entity.setExternalSourceRef(dto.getExternalSourceRef());
    }

    public Collection<EventReadDto> mapToEventReadDto(Stream<Event> events) {
        return events.map(this::mapToEventReadDto).toList();
    }

    private void setCommonEventProperties(EventWriteDto dto, Event entity) {
        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());
        entity.setDescription(dto.getDescription());
        entity.setCriticality(dto.getCriticality());
        entity.setCategory(dto.getCategory());
        entity.setEquipment(equipmentService.getEquipmentByIdOrNull(dto.getEquipmentId()));
        entity.setDomain(mapToDomain(dto.getDomain()));
    }

    private String mapToString(Domain domain) {
        return domain != null ? domain.getCode() : null;
    }

    private Domain mapToDomain(String domain) {
        if (domain == null) {
            return null;
        }
        return databaseReader.getDomainByCode(domain)
                .orElseThrow(() -> new IllegalArgumentException("Domain with code %s invalid".formatted(domain)));
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
            if (eventOp.getCategory() == Category.MANIFESTATION) {
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
