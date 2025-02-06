package com.provoly.event;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.action.AskedService;
import com.provoly.comment.CommentMapper;
import com.provoly.equipment.EquipmentService;
import com.provoly.equipment.ShortEquipmentMapper;
import com.provoly.event.dto.*;
import com.provoly.procedure.Procedure;
import com.provoly.service.Service;

import static com.provoly.service.ServiceStatus.*;

@ApplicationScoped
public class EventMapper {
    public static final String DEFAULT_SOURCE = "Hyperviseur";
    private final EquipmentService equipmentService;
    private final EventDatabaseReader databaseReader;
    private final ShortEquipmentMapper shortEquipmentMapper;
    private final CommentMapper commentMapper;

    public EventMapper(EquipmentService equipmentService,
            EventDatabaseReader databaseReader,
            ShortEquipmentMapper shortEquipmentMapper,
            CommentMapper commentMapper) {
        this.equipmentService = equipmentService;
        this.databaseReader = databaseReader;
        this.shortEquipmentMapper = shortEquipmentMapper;
        this.commentMapper = commentMapper;
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
                event.getProcedure() == null ? 0 : event.getProcedure().getEvents().size(),
                getProgressActions(event.getProcedure()),
                mapToString(event.getDomain()),
                event.getStartDate(),
                event.getEndDate(),
                event.getExternalSourceRef(),
                commentMapper.mapLastCommentToDto(event.getComments()),
                event.getComments().size(),
                event.getParent() == null ? null : new ParentReadDto(event.getParent().getId(), event.getParent().getName()),
                event.getCreator());
    }

    public EventSummaryDto mapToEventSummaryDto(Event event, List<Service> services) {
        long numberServices = services.stream().filter(service -> List.of(ASKED, IN_PROGRESS, NEW).contains(service.getStatus())).count();
        return new EventSummaryDto(event.getId(),
                event.getName(),
                event.getCriticality(),
                event.getStatus(),
                event.getLastModificationDate(),
                event.getCategory().getCode(),
                services.isEmpty() ? null : services.getFirst().getExternalId(),
                numberServices,
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
        entity.setCreationDate(dto.getCreationDate() != null ? dto.getCreationDate() : Instant.now());
        entity.setEquipment(equipmentService.getEquipmentByIdOrNull(dto.getEquipmentId()));
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        Event event = null;
        if (dto.getParent() != null) {
            event = databaseReader.getEventById(dto.getParent());
        }
        entity.setParent(event);
    }

    public List<ExportEventDto> mapToExportEventDto(List<Event> events) {
        return events
                .stream()
                .map(event -> new ExportEventDto(
                        event.getId(),
                        event.getName(),
                        event.getAddress(),
                        event.getDescription(),
                        event.getCriticality().getName(),
                        getCategory(event),
                        event.getStatus().getName(),
                        event.getLastModificationDate(),
                        event.getCreationDate(),
                        event.getCloseDate(),
                        getEquipment(event),
                        getLinkedEventsIds(event),
                        getProcedureProgress(event),
                        event.getDomain() == null ? null : event.getDomain().getName(),
                        event.getStartDate(),
                        event.getEndDate(),
                        event.getExternalSourceRef(),
                        getAskedServicesId(event),
                        event.getParent() == null ? null : event.getParent().getId(),
                        event.getCreator()))
                .toList();
    }

    public List<JournalEventDto> mapToJournalEventDto(Stream<Event> events) {
        return events
                .map(event -> new JournalEventDto(
                        event.getId(),
                        event.getName(),
                        event.getAddress(),
                        shortEquipmentMapper.mapToEquipmentShortDto(event.getEquipment()),
                        event.getCriticality(),
                        event.getStatus(),
                        event.getExternalSourceRef(),
                        getProcedureProgress(event),
                        event.getCategory().getCode(),
                        event.getSubCategory() == null ? null : event.getSubCategory().getCode(),
                        event.getCreationDate(),
                        event.getLastModificationDate(),
                        event.getProcedure() == null ? 0 : event.getProcedure().getEvents().size(),
                        event.getProcedure() == null ? null : event.getProcedure().getId(),
                        event.getDomain() == null ? null : event.getDomain().getCode(),
                        event.getCloseDate(),
                        commentMapper.mapLastCommentToDto(event.getComments()),
                        event.getExternalId()))
                .toList();
    }

    private String getCategory(Event event) {
        return event.getSubCategory() != null
                ? event.getSubCategory().getName()
                : event.getCategory().getName();
    }

    private String getEquipment(Event event) {
        return event.getEquipment() == null ? null : event.getEquipment().getName();
    }

    private float getProcedureProgress(Event event) {
        return event.getProcedure() == null
                ? 0
                : event.getProcedure().getProcedureProgress();
    }

    private List<Integer> getLinkedEventsIds(Event event) {
        return event.getProcedure() == null
                ? List.of()
                : event.getProcedure().getEvents()
                        .stream()
                        .map(Event::getId)
                        .toList();
    }

    private List<String> getAskedServicesId(Event event) {
        return event.getProcedure() == null
                ? List.of()
                : event.getProcedure().getActions()
                        .stream()
                        .filter(AskedService.class::isInstance)
                        .filter(action -> ((AskedService) action).getServiceExternalId() != null)
                        .map(action -> ((AskedService) action).getServiceExternalId())
                        .toList();
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
